package forpdateam.ru.forpda.ui.fragments.mentions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem;
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData;
import forpdateam.ru.forpda.model.AuthHolder;
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi;
import forpdateam.ru.forpda.presentation.mentions.MentionsPresenter;
import forpdateam.ru.forpda.presentation.mentions.MentionsView;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsFragment extends RecyclerFragment implements MentionsView {

    private DynamicDialogMenu<MentionsFragment, MentionItem> dialogMenu;
    private MentionsAdapter adapter;
    private PaginationHelper paginationHelper;
    private AuthHolder authHolder = App.get().Di().getAuthHolder();

    @InjectPresenter
    MentionsPresenter presenter;

    @ProvidePresenter
    MentionsPresenter providePresenter() {
        return new MentionsPresenter(
                App.get().Di().getMentionsRepository(),
                App.get().Di().getFavoritesRepository(),
                App.get().Di().getRouter(),
                App.get().Di().getLinkHandler()
        );
    }

    public MentionsFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_mentions));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        paginationHelper = new PaginationHelper(getActivity());
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dialogMenu = new DynamicDialogMenu<>();
        dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> presenter.copyLink(data));
        dialogMenu.addItem(getString(R.string.add_to_favorites), (context, data) -> presenter.addToFavorites(data));

        adapter = new MentionsAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(adapterListener);
        refreshLayout.setOnRefreshListener(() -> presenter.getMentions());
        paginationHelper.setListener(paginationListener);
    }

    @Override
    public void showMentions(MentionsData data) {
        if (data.getItems().isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_notifications)
                        .setTitle(R.string.funny_mentions_nodata_title)
                        .setDesc(R.string.funny_mentions_nodata_desc);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }

        adapter.addAll(data.getItems());
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getTitle());
        listScrollTop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (paginationHelper != null)
            paginationHelper.destroy();
    }

    @Override
    public void showItemDialogMenu(MentionItem item) {
        dialogMenu.disallowAll();
        dialogMenu.allow(0);
        if (item.isTopic() && authHolder.get().isAuth()) {
            dialogMenu.allow(1);
        }
        dialogMenu.show(getContext(), MentionsFragment.this, item);
    }

    @Override
    public void showAddFavoritesDialog(int id) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES, (dialog1, which1) -> {
                    presenter.addTopicToFavorite(id, FavoritesApi.SUB_TYPES[which1]);
                })
                .show();
    }

    @Override
    public void onAddToFavorite(boolean result) {
        Toast.makeText(getContext(), result ? getString(R.string.favorites_added) : getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
    }

    private PaginationHelper.PaginationListener paginationListener = new PaginationHelper.PaginationListener() {
        @Override
        public boolean onTabSelected(TabLayout.Tab tab) {
            return refreshLayout.isRefreshing();
        }

        @Override
        public void onSelectedPage(int pageNumber) {
            presenter.setCurrentSt(pageNumber);
            presenter.getMentions();
        }
    };

    private BaseAdapter.OnItemClickListener<MentionItem> adapterListener = new BaseAdapter.OnItemClickListener<MentionItem>() {
        @Override
        public void onItemClick(MentionItem item) {
            presenter.onItemClick(item);
        }

        @Override
        public boolean onItemLongClick(MentionItem item) {
            presenter.onItemLongClick(item);
            return false;
        }
    };
}
