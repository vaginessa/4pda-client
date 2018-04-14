package forpdateam.ru.forpda.ui.fragments.devdb.search;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.entity.remote.devdb.Brand;
import forpdateam.ru.forpda.entity.remote.devdb.DeviceSearch;
import forpdateam.ru.forpda.presentation.devdb.devices.DevicesPresenter;
import forpdateam.ru.forpda.presentation.devdb.search.SearchDevicesPresenter;
import forpdateam.ru.forpda.presentation.devdb.search.SearchDevicesView;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesAdapter;
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.device.DeviceFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener;
import forpdateam.ru.forpda.ui.views.messagepanel.AutoFitRecyclerView;

/**
 * Created by radiationx on 09.11.17.
 */

public class SearchFragment extends TabFragment implements SearchDevicesView, DevicesAdapter.OnItemClickListener<DeviceSearch.DeviceItem> {
    private DevicesAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private AutoFitRecyclerView recyclerView;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private DynamicDialogMenu<SearchFragment, DeviceSearch.DeviceItem> dialogMenu = new DynamicDialogMenu<>();

    @InjectPresenter
    SearchDevicesPresenter presenter;

    @ProvidePresenter
    SearchDevicesPresenter providePresenter() {
        return new SearchDevicesPresenter(App.get().Di().getDevDbRepository());
    }

    public SearchFragment() {
        configuration.setDefaultTitle("Поиск устройств");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_brand);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        recyclerView = (AutoFitRecyclerView) findViewById(R.id.base_list);
        contentController.setMainRefresh(refreshLayout);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCardsBackground();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(() -> presenter.refresh());

        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        recyclerView.addOnScrollListener(pauseOnScrollListener);

        adapter = new DevicesAdapter();
        recyclerView.setColumnWidth(App.get().dpToPx(144));
        recyclerView.setAdapter(adapter);
        try {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addItemDecoration(new DevicesFragment.SpacingItemDecoration(gridLayoutManager, App.px8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        adapter.setItemClickListener(this);

        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setQueryHint(getString(R.string.search_keywords));

        LinearLayout searchEditFrame = (LinearLayout) searchView.findViewById(R.id.search_edit_frame);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) searchEditFrame.getLayoutParams();
        params.leftMargin = 0;

        View searchSrcText = searchView.findViewById(R.id.search_src_text);
        searchSrcText.setPadding(0, searchSrcText.getPaddingTop(), 0, searchSrcText.getPaddingBottom());

        searchMenuItem.expandActionView();

        dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> presenter.copyLink(data));
        dialogMenu.addItem(getString(R.string.share), (context, data) -> presenter.shareLink(data));
        dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> presenter.createNote(data));
    }


    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        toolbar.inflateMenu(R.menu.qms_contacts_menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setIconifiedByDefault(true);
    }

    @Override
    public void showData(@NotNull Brand data, @NotNull String query) {
        setTitle("Поиск " + query);
        adapter.addAll(data.getDevices());
    }

    @Override
    public void showCreateNote(@NotNull String title, @NotNull String url) {
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
    }

    @Override
    public void onItemClick(DeviceSearch.DeviceItem item) {
        presenter.openDevice(item);
    }

    @Override
    public boolean onItemLongClick(DeviceSearch.DeviceItem item) {
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), SearchFragment.this, item);
        return false;
    }

}
