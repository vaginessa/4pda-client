package forpdateam.ru.forpda.ui.fragments.devdb.brand;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.devdb.Brand;
import forpdateam.ru.forpda.presentation.devdb.devices.DevicesPresenter;
import forpdateam.ru.forpda.presentation.devdb.devices.DevicesView;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener;
import forpdateam.ru.forpda.ui.views.messagepanel.AutoFitRecyclerView;

/**
 * Created by radiationx on 08.08.17.
 */

public class DevicesFragment extends TabFragment implements DevicesView, DevicesAdapter.OnItemClickListener<Brand.DeviceItem> {
    public final static String ARG_CATEGORY_ID = "CATEGORY_ID";
    public final static String ARG_BRAND_ID = "BRAND_ID";
    private SwipeRefreshLayout refreshLayout;
    private AutoFitRecyclerView recyclerView;
    private DevicesAdapter adapter;
    private DynamicDialogMenu<DevicesFragment, Brand.DeviceItem> dialogMenu = new DynamicDialogMenu<>();

    @InjectPresenter
    DevicesPresenter presenter;

    @ProvidePresenter
    DevicesPresenter providePresenter() {
        return new DevicesPresenter(App.get().Di().getDevDbRepository());
    }

    public DevicesFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_brand));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            presenter.setCategoryId(getArguments().getString(ARG_CATEGORY_ID, null));
            presenter.setBrandId(getArguments().getString(ARG_BRAND_ID, null));
        }
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
        refreshLayout.setOnRefreshListener(() -> presenter.loadBrand());

        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        recyclerView.addOnScrollListener(pauseOnScrollListener);

        adapter = new DevicesAdapter();
        adapter.setItemClickListener(this);
        recyclerView.setColumnWidth(App.get().dpToPx(144));
        recyclerView.setAdapter(adapter);
        try {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addItemDecoration(new SpacingItemDecoration(gridLayoutManager, App.px8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> presenter.copyLink(data));
        dialogMenu.addItem(getString(R.string.share), (context, data) -> presenter.shareLink(data));
        dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> presenter.createNote(data));
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.fragment_title_device_search)
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener(item -> {
                    presenter.openSearch();
                    return false;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void showData(@NotNull Brand data) {
        setTitle(data.getTitle());
        setTabTitle(data.getCatTitle() + " " + data.getTitle());
        setSubtitle(data.getCatTitle());
        adapter.addAll(data.getDevices());
    }

    @Override
    public void showCreateNote(@NotNull String title, @NotNull String url) {
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
    }

    @Override
    public void onItemClick(Brand.DeviceItem item) {
        presenter.openDevice(item);
    }

    @Override
    public boolean onItemLongClick(Brand.DeviceItem item) {
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), DevicesFragment.this, item);
        return false;
    }

    public static class SpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount = 1;
        private boolean fullWidth = false;
        private boolean includeEdge = true;
        private int spacing;
        private GridLayoutManager manager;

        public SpacingItemDecoration(GridLayoutManager manager, int spacing) {
            this.spacing = spacing;
            this.manager = manager;
        }

        public SpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        public SpacingItemDecoration(int spacing, boolean fullWidth) {
            this.spacing = spacing;
            this.fullWidth = fullWidth;
        }


        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (manager != null) {
                spanCount = manager.getSpanCount();
            }

            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                if (!fullWidth) {
                    outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)
                }
                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                if (!fullWidth) {
                    outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                    outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                }
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
