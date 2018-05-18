package forpdateam.ru.forpda.ui.fragments.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.View;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.app.history.HistoryItem;
import forpdateam.ru.forpda.presentation.history.HistoryPresenter;
import forpdateam.ru.forpda.presentation.history.HistoryView;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;

/**
 * Created by radiationx on 06.09.17.
 */

public class HistoryFragment extends RecyclerFragment implements HistoryView {


    private HistoryAdapter adapter;
    private DynamicDialogMenu<HistoryFragment, HistoryItem> dialogMenu;

    @InjectPresenter
    HistoryPresenter presenter;

    @ProvidePresenter
    HistoryPresenter providePresenter() {
        return new HistoryPresenter(
                App.get().Di().getHistoryRepository(),
                App.get().Di().getRouter(),
                App.get().Di().getLinkHandler()
        );
    }

    public HistoryFragment() {
        configuration.setUseCache(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_history));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dialogMenu = new DynamicDialogMenu<>();
        dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> presenter.copyLink(data));
        dialogMenu.addItem(getString(R.string.delete), (context, data) -> presenter.remove(data.getId()));

        adapter = new HistoryAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setItemClickListener(adapterListener);
        refreshLayout.setOnRefreshListener(() -> presenter.getHistory());
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add("Удалить историю")
                .setOnMenuItemClickListener(item -> {
                    presenter.clear();
                    return false;
                });
    }

    @Override
    public void showHistory(@NotNull List<? extends HistoryItem> history) {
        if (history.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_history)
                        .setTitle(R.string.funny_history_nodata_title)
                        .setDesc(R.string.funny_history_nodata_desc);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }
        adapter.addAll(history);
    }

    @Override
    public void showItemDialogMenu(HistoryItem item) {
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), HistoryFragment.this, item);
    }

    private BaseAdapter.OnItemClickListener<HistoryItem> adapterListener = new BaseAdapter.OnItemClickListener<HistoryItem>() {
        @Override
        public void onItemClick(HistoryItem item) {
            presenter.onItemClick(item);
        }

        @Override
        public boolean onItemLongClick(HistoryItem item) {
            presenter.onItemLongClick(item);
            return false;
        }
    };

}
