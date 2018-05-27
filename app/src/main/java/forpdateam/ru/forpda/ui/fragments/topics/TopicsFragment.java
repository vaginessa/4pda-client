package forpdateam.ru.forpda.ui.fragments.topics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import org.jetbrains.annotations.NotNull;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.entity.remote.topics.TopicItem;
import forpdateam.ru.forpda.entity.remote.topics.TopicsData;
import forpdateam.ru.forpda.model.AuthHolder;
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi;
import forpdateam.ru.forpda.presentation.topics.TopicsPresenter;
import forpdateam.ru.forpda.presentation.topics.TopicsView;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 01.03.17.
 */

public class TopicsFragment extends RecyclerFragment implements TopicsView {
    public final static String TOPICS_ID_ARG = "TOPICS_ID_ARG";

    private TopicsAdapter adapter;
    private PaginationHelper paginationHelper;
    private DynamicDialogMenu<TopicsFragment, TopicItem> dialogMenu;
    private AuthHolder authHolder = App.get().Di().getAuthHolder();

    @InjectPresenter
    TopicsPresenter presenter;

    @ProvidePresenter
    TopicsPresenter providePresenter() {
        return new TopicsPresenter(
                App.get().Di().getTopicsRepository(),
                App.get().Di().getForumRepository(),
                App.get().Di().getFavoritesRepository(),
                App.get().Di().getRouter(),
                App.get().Di().getLinkHandler()
        );
    }

    public TopicsFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_topics));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            presenter.setId(getArguments().getInt(TOPICS_ID_ARG));
        }
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
        dialogMenu.addItem(getString(R.string.copy_link), (context, data1) -> {
            String url;
            if (data1.isAnnounce()) {
                url = data1.getAnnounceUrl();
            } else {
                url = "https://4pda.ru/forum/index.php?showtopic=" + data1.getId();
            }
            Utils.copyToClipBoard(url);
        });
        dialogMenu.addItem(getString(R.string.open_theme_forum), (context, data1) -> {
            presenter.openTopicForum();
        });
        dialogMenu.addItem(getString(R.string.add_to_favorites), ((context, data1) -> {
            if (data1.isForum()) {
                openAddForumToFavoriteDialog(data1.getId());
            } else {
                openAddTopicToFavoriteDialog(data1.getId());
            }
        }));

        refreshLayout.setOnRefreshListener(() -> presenter.loadTopics());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TopicsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(adapterListener);
        paginationHelper.setListener(paginationListener);
    }

    @Override
    public void showTopics(@NotNull TopicsData data) {
        setTitle(data.getTitle());
        adapter.clear();
        if (!data.getForumItems().isEmpty())
            adapter.addSection(getString(R.string.forum_section), data.getForumItems());
        if (!data.getAnnounceItems().isEmpty())
            adapter.addSection(getString(R.string.announce_section), data.getAnnounceItems());
        if (!data.getPinnedItems().isEmpty())
            adapter.addSection(getString(R.string.pinned_section), data.getPinnedItems());
        adapter.addSection(getString(R.string.themes_section), data.getTopicItems());
        adapter.notifyDataSetChanged();
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getTitle());
        listScrollTop();
    }

    public void markRead(int topicId) {
        Log.d("SUKA", "markRead " + topicId);
        for (TopicItem item : presenter.getCurrentData().getTopicItems()) {
            if (item.getId() == topicId) {
                item.setNew(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu
                .add(R.string.open_forum)
                .setOnMenuItemClickListener(item -> {
                    presenter.openForum();
                    return true;
                });
        if (authHolder.get().isAuth()) {
            menu
                    .add(R.string.mark_read)
                    .setOnMenuItemClickListener(item -> {
                        openMarkReadDialog();
                        return true;
                    });
        }

        menu.add(R.string.fragment_title_search)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_search))
                .setOnMenuItemClickListener(item -> {
                    presenter.openSearch();
                    return true;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    public void openAddForumToFavoriteDialog(int forumId) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES, (dialog1, which1) -> {
                    presenter.addForumToFavorite(forumId, FavoritesApi.SUB_TYPES[which1]);
                })
                .show();
    }

    public void openAddTopicToFavoriteDialog(int topicId) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES, (dialog1, which1) -> {
                    presenter.addTopicToFavorite(topicId, FavoritesApi.SUB_TYPES[which1]);
                })
                .show();
    }

    public void openMarkReadDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.mark_read) + "?")
                .setPositiveButton(R.string.ok, (dialog, which) -> presenter.markRead())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onMarkRead() {
        Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddToFavorite(boolean result) {
        Toast.makeText(getContext(), result ? getString(R.string.favorites_added) : getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (paginationHelper != null)
            paginationHelper.destroy();
    }

    @Override
    public void showItemDialogMenu(TopicItem item) {
        dialogMenu.disallowAll();
        dialogMenu.allow(0);
        if (!item.isAnnounce()) {
            dialogMenu.allow(1);
            if (authHolder.get().isAuth()) {
                dialogMenu.allow(2);
            }
        }
        dialogMenu.show(getContext(), TopicsFragment.this, item);
    }

    private PaginationHelper.PaginationListener paginationListener = new PaginationHelper.PaginationListener() {
        @Override
        public boolean onTabSelected(TabLayout.Tab tab) {
            return refreshLayout.isRefreshing();
        }

        @Override
        public void onSelectedPage(int pageNumber) {
            presenter.loadPage(pageNumber);
        }
    };

    private TopicsAdapter.OnItemClickListener<TopicItem> adapterListener = new TopicsAdapter.OnItemClickListener<TopicItem>() {
        @Override
        public void onItemClick(TopicItem item) {
            presenter.onItemClick(item);
        }

        @Override
        public boolean onItemLongClick(TopicItem item) {
            presenter.onItemLongClick(item);
            return false;
        }
    };
}
