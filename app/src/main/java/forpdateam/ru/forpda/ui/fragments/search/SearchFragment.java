package forpdateam.ru.forpda.ui.fragments.search;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient;
import forpdateam.ru.forpda.common.webview.CustomWebViewClient;
import forpdateam.ru.forpda.entity.remote.IBaseForumPost;
import forpdateam.ru.forpda.entity.remote.search.SearchItem;
import forpdateam.ru.forpda.entity.remote.search.SearchResult;
import forpdateam.ru.forpda.entity.remote.search.SearchSettings;
import forpdateam.ru.forpda.model.AuthHolder;
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi;
import forpdateam.ru.forpda.presentation.search.SearchPresenter;
import forpdateam.ru.forpda.presentation.search.SearchSiteView;
import forpdateam.ru.forpda.presentation.theme.ThemeJsInterface;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.fragments.theme.ThemeDialogsHelper_V2;
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.ExtendedWebView;
import forpdateam.ru.forpda.ui.views.FabOnScroll;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener;
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

/**
 * Created by radiationx on 29.01.17.
 */

public class SearchFragment extends TabFragment implements SearchSiteView, ExtendedWebView.JsLifeCycleListener, SearchAdapter.OnItemClickListener<SearchItem> {

    private final static String LOG_TAG = SearchFragment.class.getSimpleName();

    private boolean scrollButtonEnable = false;

    private ViewGroup searchSettingsView;
    private ViewGroup nickBlock, resourceBlock, resultBlock, sortBlock, sourceBlock;
    private Spinner resourceSpinner, resultSpinner, sortSpinner, sourceSpinner;
    private TextView nickField;
    private Button submitButton, saveSettingsButton;


    private ExtendedWebView webView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private SearchAdapter adapter = new SearchAdapter();
    private CustomWebViewClient webViewClient;


    private PaginationHelper paginationHelper;
    private DynamicDialogMenu<SearchFragment, IBaseForumPost> dialogMenu;


    private SearchView searchView;
    private MenuItem searchItem;
    private BottomSheetDialog dialog;
    private SimpleTooltip tooltip;

    private MenuItem settingsMenuItem;


    private ThemeJsInterface jsInterface;
    private ThemeDialogsHelper_V2 dialogsHelper;

    private AuthHolder authHolder = App.get().Di().getAuthHolder();


    private Observer searchPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Theme.SHOW_AVATARS: {
                updateShowAvatarState(Preferences.Theme.isShowAvatars(getContext()));
                break;
            }
            case Preferences.Theme.CIRCLE_AVATARS: {
                updateTypeAvatarState(Preferences.Theme.isCircleAvatars(getContext()));
                break;
            }
            case Preferences.Main.WEBVIEW_FONT_SIZE: {
                webView.setRelativeFontSize(Preferences.Main.getWebViewSize(getContext()));
            }
            case Preferences.Main.SCROLL_BUTTON_ENABLE: {
                scrollButtonEnable = Preferences.Main.isScrollButtonEnable(getContext());
                if (scrollButtonEnable) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.GONE);
                }
            }
        }
    };

    protected void updateShowAvatarState(boolean isShow) {
        webView.evalJs("updateShowAvatarState(" + isShow + ")");
    }

    protected void updateTypeAvatarState(boolean isCircle) {
        webView.evalJs("updateTypeAvatarState(" + isCircle + ")");
    }

    @InjectPresenter
    SearchPresenter presenter;

    @ProvidePresenter
    SearchPresenter providePresenter() {
        return new SearchPresenter(
                App.get().Di().getSearchRepository(),
                App.get().Di().getFavoritesRepository(),
                App.get().Di().getThemeRepository(),
                App.get().Di().getReputationRepository(),
                App.get().Di().getSearchTemplate(),
                App.get().Di().getRouter(),
                App.get().Di().getLinkHandler()
        );
    }


    public SearchFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_search));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        scrollButtonEnable = Preferences.Main.isScrollButtonEnable(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String searchUrl = App.get().getPreferences().getString("search_settings", null);
        if (getArguments() != null) {
            searchUrl = getArguments().getString(TabFragment.ARG_TAB);
        }
        presenter.initSearchSettings(searchUrl);
        dialogsHelper = new ThemeDialogsHelper_V2(getContext(), authHolder);
    }

    @Override
    protected void initFabBehavior() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        FabOnScroll behavior = new FabOnScroll(fab.getContext());
        params.setBehavior(behavior);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            params.setMargins(App.px16, App.px16, App.px16, App.px16);
        }
        fab.requestLayout();
    }

    @SuppressLint("JavascriptInterface")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initFabBehavior();

        baseInflateFragment(inflater, R.layout.fragment_search);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        searchSettingsView = (ViewGroup) View.inflate(getContext(), R.layout.search_settings, null);

        nickBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_nick_block);
        resourceBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_resource_block);
        resultBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_result_block);
        sortBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_sort_block);
        sourceBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_source_block);

        resourceSpinner = (Spinner) searchSettingsView.findViewById(R.id.search_resource_spinner);
        resultSpinner = (Spinner) searchSettingsView.findViewById(R.id.search_result_spinner);
        sortSpinner = (Spinner) searchSettingsView.findViewById(R.id.search_sort_spinner);
        sourceSpinner = (Spinner) searchSettingsView.findViewById(R.id.search_source_spinner);

        nickField = (TextView) searchSettingsView.findViewById(R.id.search_nick_field);

        submitButton = (Button) searchSettingsView.findViewById(R.id.search_submit);
        saveSettingsButton = (Button) searchSettingsView.findViewById(R.id.search_save_settings);

        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        attachWebView(webView);
        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        refreshLayout.addView(recyclerView);

        paginationHelper = new PaginationHelper(getActivity());
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow());

        contentController.setMainRefresh(refreshLayout);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        jsInterface = new ThemeJsInterface(presenter);

        dialogMenu = new DynamicDialogMenu<>();
        dialogMenu.addItem(getString(R.string.topic_to_begin), (context, data1) -> presenter.openTopicBegin(data1));
        dialogMenu.addItem(getString(R.string.topic_newposts), (context, data1) -> presenter.openTopicNew(data1));
        dialogMenu.addItem(getString(R.string.topic_lastposts), (context, data1) -> presenter.openTopicLast(data1));
        dialogMenu.addItem(getString(R.string.copy_link), (context, data1) -> presenter.copyLink(data1));
        dialogMenu.addItem(getString(R.string.open_theme_forum), (context, data1) -> presenter.openForum(data1));
        dialogMenu.addItem(getString(R.string.add_to_favorites), ((context, data1) -> presenter.onClickAddInFav(data1)));

        fab.setOnClickListener(v -> {
            if (webView.getDirection() == ExtendedWebView.DIRECTION_DOWN) {
                webView.pageDown(true);
            } else if (webView.getDirection() == ExtendedWebView.DIRECTION_UP) {
                webView.pageUp(true);
            }
        });
        webView.setOnDirectionListener(direction -> {
            if (webView.getDirection() == ExtendedWebView.DIRECTION_DOWN) {
                fab.setImageDrawable(App.getVecDrawable(fab.getContext(), R.drawable.ic_arrow_down));
            } else if (webView.getDirection() == ExtendedWebView.DIRECTION_UP) {
                fab.setImageDrawable(App.getVecDrawable(fab.getContext(), R.drawable.ic_arrow_up));
            }
        });

        webView.setJsLifeCycleListener(this);
        webView.addJavascriptInterface(jsInterface, ThemeFragmentWeb.JS_INTERFACE);
        webView.setRelativeFontSize(Preferences.Main.getWebViewSize(getContext()));

        fab.setSize(FloatingActionButton.SIZE_MINI);
        if (scrollButtonEnable) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        fab.setScaleX(0.0f);
        fab.setScaleY(0.0f);
        fab.setAlpha(0.0f);

        setCardsBackground();
        App.get().addPreferenceChangeObserver(searchPreferenceObserver);

        paginationHelper.setListener(new PaginationHelper.PaginationListener() {
            @Override
            public boolean onTabSelected(TabLayout.Tab tab) {
                return refreshLayout.isRefreshing();
            }

            @Override
            public void onSelectedPage(int pageNumber) {
                presenter.search(pageNumber);
            }
        });

        //searchSettingsView.setVisibility(View.GONE);
        dialog = new BottomSheetDialog(getContext());
        //dialog.setPeekHeight(App.getKeyboardHeight());
        //dialog.getWindow().getDecorView().setFitsSystemWindows(true);


        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearch();
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


        searchItem.expandActionView();
        submitButton.setOnClickListener(v -> startSearch());
        saveSettingsButton.setOnClickListener(v -> presenter.saveSettings());
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DevicesFragment.SpacingItemDecoration(App.px8, true));
        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        recyclerView.addOnScrollListener(pauseOnScrollListener);
        recyclerView.setAdapter(adapter);
        refreshLayoutStyle(refreshLayout);
        refreshLayoutLongTrigger(refreshLayout);
        refreshLayout.setOnRefreshListener(() -> presenter.refreshData());
        adapter.setOnItemClickListener(this);

        if (App.get().getPreferences().getBoolean("search.tooltip.settings", true)) {
            for (int toolbarChildIndex = 0; toolbarChildIndex < toolbar.getChildCount(); toolbarChildIndex++) {
                View childView = toolbar.getChildAt(toolbarChildIndex);
                if (childView instanceof ActionMenuView) {
                    ActionMenuView menuView = (ActionMenuView) childView;
                    for (int menuChildIndex = 0; menuChildIndex < menuView.getChildCount(); menuChildIndex++) {
                        try {
                            ActionMenuItemView itemView = (ActionMenuItemView) menuView.getChildAt(menuChildIndex);
                            if (settingsMenuItem == itemView.getItemData()) {
                                tooltip = new SimpleTooltip.Builder(getContext())
                                        .anchorView(itemView)
                                        .text(R.string.tooltip_search_settings)
                                        .gravity(Gravity.BOTTOM)
                                        .animated(false)
                                        .modal(true)
                                        .transparentOverlay(false)
                                        .backgroundColor(Color.BLACK)
                                        .textColor(Color.WHITE)
                                        .padding((float) App.px16)
                                        .build();
                                tooltip.show();
                                break;
                            }
                        } catch (ClassCastException ignore) {
                        }
                    }
                    break;
                }
            }

            App.get().getPreferences().edit().putBoolean("search.tooltip.settings", false).apply();
        }


    }


    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.copy_link)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.copyLink();
                    return false;
                });
        toolbar.inflateMenu(R.menu.qms_contacts_menu);

        settingsMenuItem = menu.add(R.string.settings)
                .setIcon(R.drawable.ic_toolbar_tune).setOnMenuItemClickListener(menuItem -> {
                    hidePopupWindows();
                    if (searchSettingsView != null && searchSettingsView.getParent() != null && searchSettingsView.getParent() instanceof ViewGroup) {
                        ((ViewGroup) searchSettingsView.getParent()).removeView(searchSettingsView);
                    }
                    if (searchSettingsView != null) {
                        dialog.setContentView(searchSettingsView);
                        dialog.show();
                    }
                    return false;
                });
        settingsMenuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(true);
    }

    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        if (tooltip != null && tooltip.isShowing()) {
            tooltip.dismiss();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void showAddInFavDialog(@NotNull IBaseForumPost item) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES, (dialog1, which1) -> {
                    presenter.addTopicToFavorite(item.getTopicId(), FavoritesApi.SUB_TYPES[which1]);
                })
                .show();
    }

    @Override
    public void onAddToFavorite(boolean result) {
        Toast.makeText(getContext(), result ? getString(R.string.favorites_added) : getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
        refreshToolbarMenuItems(true);
    }

    private boolean checkArg(String arg, Pair<String, String> pair) {
        return arg.equals(pair.first);
    }

    private void setSelection(Spinner spinner, List<String> items, Pair<String, String> pair) {
        spinner.setSelection(items.indexOf(pair.second));
    }

    private void setItems(Spinner spinner, List<String> items, int selection) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getMainActivity(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selection);
        spinner.setOnItemSelectedListener(listener);
    }

    @Override
    public void fillSettingsData(@NotNull SearchSettings settings, @NotNull Map<String, ? extends List<String>> fields) {
        searchView.post(() -> {
            searchView.setQuery(settings.getQuery(), false);
        });

        nickField.setText(settings.getNick());

        List<String> resourceItems = fields.get(SearchPresenter.FIELD_RESOURCE);
        List<String> resultItems = fields.get(SearchPresenter.FIELD_RESULT);
        List<String> sortItems = fields.get(SearchPresenter.FIELD_SORT);
        List<String> sourceItems = fields.get(SearchPresenter.FIELD_SOURCE);

        setItems(resourceSpinner, resourceItems, 0);
        setItems(resultSpinner, resultItems, 0);
        setItems(sortSpinner, sortItems, 0);
        setItems(sourceSpinner, sourceItems, 1);

        if (checkArg(settings.getResourceType(), SearchSettings.RESOURCE_NEWS)) {
            setSelection(resourceSpinner, resourceItems, SearchSettings.RESOURCE_NEWS);
        } else if (checkArg(settings.getResourceType(), SearchSettings.RESOURCE_FORUM)) {
            setSelection(resourceSpinner, resourceItems, SearchSettings.RESOURCE_FORUM);
        }

        if (checkArg(settings.getResult(), SearchSettings.RESULT_TOPICS)) {
            setSelection(resultSpinner, resultItems, SearchSettings.RESULT_TOPICS);
        } else if (checkArg(settings.getResult(), SearchSettings.RESULT_POSTS)) {
            setSelection(resultSpinner, resultItems, SearchSettings.RESULT_POSTS);
        }

        if (checkArg(settings.getSort(), SearchSettings.SORT_DA)) {
            setSelection(sortSpinner, sortItems, SearchSettings.SORT_DA);
        } else if (checkArg(settings.getSort(), SearchSettings.SORT_DD)) {
            setSelection(sortSpinner, sortItems, SearchSettings.SORT_DD);
        } else if (checkArg(settings.getSort(), SearchSettings.SORT_REL)) {
            setSelection(sortSpinner, sortItems, SearchSettings.SORT_REL);
        }

        if (checkArg(settings.getSource(), SearchSettings.SOURCE_ALL)) {
            setSelection(sourceSpinner, sourceItems, SearchSettings.SOURCE_ALL);
        } else if (checkArg(settings.getSource(), SearchSettings.SOURCE_TITLES)) {
            setSelection(sourceSpinner, sourceItems, SearchSettings.SOURCE_TITLES);
        } else if (checkArg(settings.getSource(), SearchSettings.SOURCE_CONTENT)) {
            setSelection(sourceSpinner, sourceItems, SearchSettings.SOURCE_CONTENT);
        }
    }


    private AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String field = null;
            if (parent == resourceSpinner) {
                field = SearchPresenter.FIELD_RESOURCE;
            } else if (parent == resultSpinner) {
                field = SearchPresenter.FIELD_RESULT;
            } else if (parent == sortSpinner) {
                field = SearchPresenter.FIELD_SORT;
            } else if (parent == sourceSpinner) {
                field = SearchPresenter.FIELD_SOURCE;
            }

            if (field != null) {
                presenter.updateSettings(field, position);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public void setNewsMode() {
        nickBlock.setVisibility(View.GONE);
        resultBlock.setVisibility(View.GONE);
        sortBlock.setVisibility(View.GONE);
        sourceBlock.setVisibility(View.GONE);
    }

    @Override
    public void setForumMode() {
        nickBlock.setVisibility(View.VISIBLE);
        resultBlock.setVisibility(View.VISIBLE);
        sortBlock.setVisibility(View.VISIBLE);
        sourceBlock.setVisibility(View.VISIBLE);
    }

    private void startSearch() {
        presenter.search(searchView.getQuery().toString(), nickField.getText().toString());
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onStartSearch(@NotNull SearchSettings settings) {
        hidePopupWindows();

        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append("Поиск");
        if (settings.getResourceType().equals(SearchSettings.RESOURCE_NEWS.first)) {
            titleBuilder.append(" новостей");
        } else {
            if (settings.getResult().equals(SearchSettings.RESULT_POSTS.first)) {
                titleBuilder.append(" сообщений");
            } else {
                titleBuilder.append(" тем");
            }
            if (!settings.getNick().isEmpty()) {
                titleBuilder.append(" пользователя \"").append(settings.getNick()).append("\"");
            }
        }
        if (!settings.getQuery().isEmpty()) {
            titleBuilder.append(" по запросу \"").append(settings.getQuery()).append("\"");
        }
        setTitle(titleBuilder.toString());
    }

    @Override
    public void showData(@NotNull SearchResult searchResult) {
        setRefreshing(false);
        recyclerView.scrollToPosition(0);
        hidePopupWindows();
        Log.d("SUKA", "SEARCH SIZE " + searchResult.getItems().size());
        if (searchResult.getItems().isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_search)
                        .setTitle(R.string.funny_search_nodata_title)
                        .setDesc(R.string.funny_search_nodata_desc);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }
        if (searchResult.getSettings().getResult().equals(SearchSettings.RESULT_POSTS.first) && searchResult.getSettings().getResourceType().equals(SearchSettings.RESOURCE_FORUM.first)) {
            for (int i = 0; i < refreshLayout.getChildCount(); i++) {
                if (refreshLayout.getChildAt(i) instanceof RecyclerView) {
                    refreshLayout.removeViewAt(i);
                    fixTargetView();
                    break;
                }
            }
            if (refreshLayout.getChildCount() <= 1) {
                if (scrollButtonEnable) {
                    fab.setVisibility(View.VISIBLE);
                }
                refreshLayout.addView(webView);
                Log.d(LOG_TAG, "add webview");
            }
            if (webViewClient == null) {
                webViewClient = new CustomWebViewClient();
                webView.setWebViewClient(webViewClient);
                webView.setWebChromeClient(new CustomWebChromeClient());
            }
            Log.d("SUKA", "SEARCH SHOW WEBVIEW");
            webView.loadDataWithBaseURL("https://4pda.ru/forum/", searchResult.getHtml(), "text/html", "utf-8", null);
        } else {
            for (int i = 0; i < refreshLayout.getChildCount(); i++) {
                if (refreshLayout.getChildAt(i) instanceof ExtendedWebView) {
                    refreshLayout.removeViewAt(i);
                    fixTargetView();
                }
            }
            if (refreshLayout.getChildCount() <= 1) {
                fab.setVisibility(View.GONE);
                refreshLayout.addView(recyclerView);
                Log.d(LOG_TAG, "add recyclerview");
            }
            Log.d("SUKA", "SEARCH SHOW RECYCLERVIEW");
            adapter.clear();
            adapter.addAll(searchResult.getItems());
        }

        paginationHelper.updatePagination(searchResult.getPagination());
        setSubtitle(paginationHelper.getTitle());
    }


    //Поле mTarget это вьюха, от которой зависит обработка движений
    private void fixTargetView() {
        try {
            Field field = refreshLayout.getClass().getDeclaredField("mTarget");
            field.setAccessible(true);
            field.set(refreshLayout, null);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.get().removePreferenceChangeObserver(searchPreferenceObserver);
        unregisterForContextMenu(webView);
        webView.removeJavascriptInterface(ThemeFragmentWeb.JS_INTERFACE);
        webView.setJsLifeCycleListener(null);
        webView.endWork();
        getMainActivity().getWebViewsProvider().push(webView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (paginationHelper != null)
            paginationHelper.destroy();
    }

    @Override
    public void onDomContentComplete(final ArrayList<String> actions) {
    }

    @Override
    public void onPageComplete(final ArrayList<String> actions) {
        actions.add("window.scrollTo(0, 0);");
    }

    @Override
    public void onItemClick(SearchItem item) {
        presenter.onItemClick(item);
    }

    @Override
    public boolean onItemLongClick(SearchItem item) {
        presenter.onItemLongClick(item);
        return false;
    }

    @Override
    public void showItemDialogMenu(@NotNull SearchItem item, @NotNull SearchSettings settings) {
        dialogMenu.disallowAll();
        if (settings.getResourceType().equals(SearchSettings.RESOURCE_NEWS.first)) {
            dialogMenu.allow(3);
        } else {
            dialogMenu.allowAll();
        }
        dialogMenu.show(getContext(), SearchFragment.this, item);
    }


    /* JS PRESENTER */

    @Override
    public void showNoteCreate(@NotNull String title, @NotNull String url) {
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
    }

    @Override
    public void deletePostUi(@NotNull IBaseForumPost post) {
        webView.evalJs("onDeletePostClick(" + post.getId() + ");");
    }

    @Override
    public void openAnchorDialog(@NotNull IBaseForumPost post, @NotNull String anchorName) {
        dialogsHelper.openAnchorDialog(presenter, post, anchorName);
    }

    @Override
    public void openSpoilerLinkDialog(@NotNull IBaseForumPost post, @NotNull String spoilNumber) {
        dialogsHelper.openSpoilerLinkDialog(presenter, post, spoilNumber);
    }

    @Override
    public void firstPage() {
        paginationHelper.firstPage();
    }

    @Override
    public void prevPage() {
        paginationHelper.prevPage();
    }

    @Override
    public void nextPage() {
        paginationHelper.nextPage();
    }

    @Override
    public void lastPage() {
        paginationHelper.lastPage();
    }

    @Override
    public void selectPage() {
        paginationHelper.selectPageDialog();
    }


    public void toast(@NotNull final String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void log(@NotNull String text) {
        int maxLogSize = 1000;
        for (int i = 0; i <= text.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > text.length() ? text.length() : end;
            Log.v(LOG_TAG, text.substring(start, end));
        }
    }

    @Override
    public void showUserMenu(@NotNull IBaseForumPost post) {
        dialogsHelper.showUserMenu(presenter, post);
    }

    @Override
    public void showReputationMenu(@NotNull IBaseForumPost post) {
        dialogsHelper.showReputationMenu(presenter, post);
    }

    @Override
    public void showPostMenu(@NotNull IBaseForumPost post) {
        dialogsHelper.showPostMenu(presenter, post);
    }

    @Override
    public void reportPost(@NotNull IBaseForumPost post) {
        dialogsHelper.tryReportPost(presenter, post);
    }

    @Override
    public void deletePost(@NotNull IBaseForumPost post) {
        dialogsHelper.deletePost(presenter, post);
    }

    @Override
    public void votePost(@NotNull IBaseForumPost post, boolean type) {
        dialogsHelper.votePost(presenter, post, type);
    }

    @Override
    public void showChangeReputation(@NotNull IBaseForumPost post, boolean type) {
        dialogsHelper.changeReputation(presenter, post, type);
    }

    @Override
    public void editPost(@NotNull IBaseForumPost post) {
        presenter.openEditPostForm(post.getId());
    }

}
