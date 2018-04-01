package forpdateam.ru.forpda.ui.fragments.news.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.news.NewsItem;
import forpdateam.ru.forpda.presentation.articles.list.ArticlesListPresenter;
import forpdateam.ru.forpda.presentation.articles.list.ArticlesListView;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener;

/**
 * Created by isanechek on 8/8/17.
 */

public class NewsMainFragment extends RecyclerFragment implements NewsListAdapter.ItemClickListener, ArticlesListView {
    private NewsListAdapter adapter;
    private DynamicDialogMenu<NewsMainFragment, NewsItem> dialogMenu = new DynamicDialogMenu<>();

    @InjectPresenter
    ArticlesListPresenter presenter;

    @ProvidePresenter
    ArticlesListPresenter provideMentionsPresenter() {
        return new ArticlesListPresenter(App.get().Di().getNewsRepository());
    }

    public NewsMainFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_news_list));
        configuration.setAlone(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCardsBackground();
        refreshLayout.setOnRefreshListener(() -> presenter.refreshArticles());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px8, true));
        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        recyclerView.addOnScrollListener(pauseOnScrollListener);
        adapter = new NewsListAdapter();
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);

        dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> presenter.copyLink(data));
        dialogMenu.addItem(getString(R.string.share), (context, data) -> presenter.shareLink(data));
        dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> presenter.createNote(data));
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.fragment_title_search)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_search))
                .setOnMenuItemClickListener(item -> {
                    presenter.openSearch();
                    return true;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void onLoadMoreClick() {
        presenter.loadMore();
    }

    @Override
    public void showNews(@NotNull List<? extends NewsItem> items, boolean withClear) {
        if (withClear) {
            if (!items.isEmpty()) {
                adapter.clear();
                adapter.addAll(items);
            }
        } else adapter.insertMore(items);
    }

    @Override
    public void showCreateNote(@NotNull String title, @NotNull String url) {
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
    }

    @Override
    public void showItemDialogMenu(@NotNull NewsItem item) {
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), NewsMainFragment.this, item);
    }

    @Override
    public void onItemClick(View view, NewsItem item, int position) {
        presenter.onItemClick(item);
    }

    @Override
    public boolean onLongItemClick(View view, NewsItem item, int position) {
        presenter.onItemLongClick(item);
        return true;
    }

    @Override
    public void onNickClick(View view, NewsItem item, int position) {
        presenter.openProfile(item);
    }
}
