package forpdateam.ru.forpda.ui.fragments.theme;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.IBaseForumPost;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.FilePickHelper;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.presentation.theme.ThemePresenter;
import forpdateam.ru.forpda.presentation.theme.ThemeView;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesHelper;
import forpdateam.ru.forpda.ui.fragments.history.HistoryFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment;
import forpdateam.ru.forpda.ui.views.FabOnScroll;
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.ui.views.messagepanel.attachments.AttachmentsPopup;
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

/**
 * Created by radiationx on 20.10.16.
 */

public abstract class ThemeFragment extends TabFragment implements ThemeView {
    //Указывают на произведенное действие: переход назад, обновление, обычный переход по ссылке
    private final static String LOG_TAG = ThemeFragment.class.getSimpleName();

    private ThemeDialogsHelper_V2 dialogsHelper;

    protected MenuItem toggleMessagePanelItem;
    protected MenuItem refreshMenuItem;
    protected MenuItem copyLinkMenuItem;
    protected MenuItem searchOnPageMenuItem;
    protected MenuItem searchInThemeMenuItem;
    protected MenuItem searchPostsMenuItem;
    protected MenuItem deleteFavoritesMenuItem;
    protected MenuItem addFavoritesMenuItem;
    protected MenuItem openForumMenuItem;

    protected SwipeRefreshLayout refreshLayout;

    private PaginationHelper paginationHelper;

    //Тег для вьюхи поиска. Чтобы создавались кнопки и т.д, только при вызове поиска, а не при каждом создании меню.
    protected int searchViewTag = 0;

    protected MessagePanel messagePanel;
    protected AttachmentsPopup attachmentsPopup;

    protected SimpleTooltip tooltip;

    private View notificationView;
    private TextView notificationTitle;
    private ImageButton notificationButton;
    private Handler notificationHandler = new Handler(Looper.getMainLooper());
    private Runnable notifyRunnable = () -> notificationView.setVisibility(View.VISIBLE);

    private Observer themePreferenceObserver = (observable, o) -> {
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
                setFontSize(Preferences.Main.getWebViewSize(getContext()));
            }
            case Preferences.Main.SCROLL_BUTTON_ENABLE: {
                if (Preferences.Main.isScrollButtonEnable(getContext())) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.GONE);
                }
            }
        }
    };

    private Observer notification = (observable, o) -> {
        if (o == null) return;
        TabNotification event = (TabNotification) o;
        runInUiThread(() -> handleEvent(event));
    };


    @InjectPresenter
    ThemePresenter presenter;

    @ProvidePresenter
    ThemePresenter provideThemePresenter() {
        return new ThemePresenter(
                App.get().Di().getThemeRepository(),
                App.get().Di().getReputationRepository()
        );
    }


    private void handleEvent(TabNotification event) {
        Log.e("SUKAT", "handleEvent " + event.isWebSocket() + " : " + event.getSource() + " : " + event.getType());
        if (!event.isWebSocket())
            return;
        if (!presenter.isPageLoaded())
            return;
        Log.e("SUKAT", "handleEvent " + event.getEvent().getSourceId() + " : " + presenter.getId());
        if (event.getEvent().getSourceId() != presenter.getId())
            return;

        if (event.getSource() == NotificationEvent.Source.THEME) {
            switch (event.getType()) {
                case NEW:
                    onEventNew(event);
                    break;
                case READ:
                    onEventRead(event);
                    break;
                case MENTION:

                    break;
            }
        }
    }

    private void onEventNew(TabNotification event) {
        Log.d("SUKAT", "onEventNew");
        notificationHandler.postDelayed(notifyRunnable, 2000);

    }

    private void onEventRead(TabNotification event) {
        Log.d("SUKAT", "onEventRead");
        notificationHandler.removeCallbacks(notifyRunnable);
        notificationView.setVisibility(View.GONE);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            presenter.setThemeUrl(getArguments().getString(ARG_TAB, ""));
        }
        dialogsHelper = new ThemeDialogsHelper_V2(getContext());
    }

    @Override
    protected void initFabBehavior() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        FabOnScroll behavior = new FabOnScroll(fab.getContext(), null);
        params.setBehavior(behavior);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            params.setMargins(App.px16, App.px16, App.px16, App.px16);
        }
        fab.requestLayout();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initFabBehavior();
        baseInflateFragment(inflater, R.layout.fragment_theme);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        messagePanel = new MessagePanel(getContext(), fragmentContainer, coordinatorLayout, false);
        paginationHelper = new PaginationHelper(getActivity());
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow());

        notificationView = inflater.inflate(R.layout.new_message_notification, null);
        notificationTitle = (TextView) notificationView.findViewById(R.id.title);
        notificationButton = (ImageButton) notificationView.findViewById(R.id.icon);
        fragmentContent.addView(notificationView);
        notificationView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        contentController.setMainRefresh(refreshLayout);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFontSize(Preferences.Main.getWebViewSize(getContext()));

        notificationButton.setColorFilter(App.getColorFromAttr(getContext(), R.attr.contrast_text_color), PorterDuff.Mode.SRC_ATOP);
        notificationTitle.setText("Новое сообщение");
        notificationView.setVisibility(View.GONE);
        notificationButton.setOnClickListener(v -> {
            notificationView.setVisibility(View.GONE);
        });
        notificationView
                .findViewById(R.id.new_message_card)
                .setOnClickListener(v -> {
                    presenter.loadNewPosts();
                    notificationView.setVisibility(View.GONE);
                });

        messagePanel.enableBehavior();
        messagePanel.addSendOnClickListener(v -> sendMessage());
        messagePanel.getSendButton().setOnLongClickListener(v -> {
            presenter.openEditPostForm(messagePanel.getMessage(), messagePanel.getAttachments());
            return true;
        });
        messagePanel.getFullButton().setVisibility(View.VISIBLE);
        messagePanel.getFullButton().setOnClickListener(v -> {
            presenter.openEditPostForm(messagePanel.getMessage(), messagePanel.getAttachments());
        });
        messagePanel.getHideButton().setVisibility(View.VISIBLE);
        messagePanel.getHideButton().setOnClickListener(v -> {
            hideMessagePanel();
        });
        attachmentsPopup = messagePanel.getAttachmentsPopup();
        attachmentsPopup.setAddOnClickListener(v -> tryPickFile());
        attachmentsPopup.setDeleteOnClickListener(v -> removeFiles());


        paginationHelper.setListener(new PaginationHelper.PaginationListener() {
            @Override
            public boolean onTabSelected(TabLayout.Tab tab) {
                return refreshLayout.isRefreshing();
            }

            @Override
            public void onSelectedPage(int pageNumber) {
                presenter.loadPage(pageNumber);
            }
        });

        fab.setSize(FloatingActionButton.SIZE_MINI);
        if (Preferences.Main.isScrollButtonEnable(getContext())) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        fab.setScaleX(0.0f);
        fab.setScaleY(0.0f);
        fab.setAlpha(0.0f);

        App.get().addPreferenceChangeObserver(themePreferenceObserver);
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(() -> {
            presenter.reload();
        });
        if (App.get().getPreferences().getBoolean("theme.tooltip.long_click_send", true)) {
            tooltip = new SimpleTooltip.Builder(getContext())
                    .anchorView(messagePanel.getSendButton())
                    .text(R.string.tooltip_full_form)
                    .gravity(Gravity.TOP)
                    .animated(false)
                    .modal(true)
                    .transparentOverlay(false)
                    .backgroundColor(Color.BLACK)
                    .textColor(Color.WHITE)
                    .padding((float) App.px16)
                    .build();
            tooltip.show();
            App.get().getPreferences().edit().putBoolean("theme.tooltip.long_click_send", false).apply();
        }

        if (Preferences.Main.isEditorDefaultHidden(getContext())) {
            hideMessagePanel();
        } else {
            showMessagePanel(false);
        }
        App.get().subscribeFavorites(notification);
    }

    @Override
    public void onResume() {
        super.onResume();
        messagePanel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        messagePanel.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.get().removePreferenceChangeObserver(themePreferenceObserver);
        App.get().unSubscribeFavorites(notification);
        messagePanel.onDestroy();
        if (paginationHelper != null)
            paginationHelper.destroy();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        messagePanel.hidePopupWindows();
    }

    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        if (tooltip != null && tooltip.isShowing()) {
            tooltip.dismiss();
            return true;
        }

        if (messagePanel.onBackPressed()) {
            return true;
        }

        if (getMenu().findItem(R.id.action_search) != null && getMenu().findItem(R.id.action_search).isActionViewExpanded()) {
            toolbar.collapseActionView();
            return true;
        }

        if (presenter.onBackPressed()) {
            return true;
        }

        if ((messagePanel.getMessage() != null && !messagePanel.getMessage().isEmpty()) || !messagePanel.getAttachments().isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.editpost_lose_changes)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        TabManager.get().remove(ThemeFragment.this);
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
            return true;
        }

        return false;
    }


    @Override
    public void setRefreshing(boolean isRefreshing) {
        super.setRefreshing(isRefreshing);
        refreshToolbarMenuItems(!isRefreshing);
    }

    @Override
    public void onLoadData(@NotNull ThemePage newPage) {
        appBarLayout.post(() -> {
            if (appBarLayout != null) {
                appBarLayout.setExpanded(false, true);
            }
        });

        updateFavorites(newPage);
        updateMainHistory(newPage);
        updateView(newPage);
    }

    @CallSuper
    @Override
    public void updateView(@NotNull ThemePage page) {
        refreshToolbarMenuItems(true);
        paginationHelper.updatePagination(page.getPagination());

        setTitle(page.getTitle());

        setTabTitle(String.format(getString(R.string.fragment_tab_title_theme), page.getTitle()));

        Pagination pagination = page.getPagination();
        setSubtitle("" + pagination.getCurrent() + "/" + pagination.getAll());
    }

    protected void updateFavorites(ThemePage themePage) {
        if (!ClientHelper.getAuthState()
                || themePage.getPagination().getCurrent() < themePage.getPagination().getAll())
            return;

        int topicId = themePage.getId();

        TabFragment parentTab = TabManager.get().get(getParentTag());
        if (parentTab == null) {
            parentTab = TabManager.get().getByClass(FavoritesFragment.class);
        }

        if (parentTab == null)
            return;

        if (parentTab instanceof FavoritesFragment) {
            ((FavoritesFragment) parentTab).markRead(topicId);
        } else if (parentTab instanceof TopicsFragment) {
            ((TopicsFragment) parentTab).markRead(topicId);
        }
    }

    protected void updateMainHistory(ThemePage themePage) {
        long time = System.currentTimeMillis();
        HistoryFragment.addToHistory(themePage.getId(), themePage.getUrl(), themePage.getTitle());
        Log.d("SUKA", "ADD TO HISTORY " + (System.currentTimeMillis() - time));
    }


    private void toggleMessagePanel() {
        if (messagePanel.getVisibility() == View.VISIBLE) {
            hideMessagePanel();
        } else {
            showMessagePanel(true);
        }
    }

    private void showMessagePanel(boolean showKeyboard) {
        if (messagePanel.getVisibility() != View.VISIBLE) {
            messagePanel.setVisibility(View.VISIBLE);
            if (showKeyboard) {
                messagePanel.show();
            }
            messagePanel.getHeightChangeListener().onChangedHeight(messagePanel.getLastHeight());
            toggleMessagePanelItem.setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_transcribe_close));
        }
        if (showKeyboard) {
            //messagePanel.getMessageField().setSelection(messagePanel.getMessageField().length());
            messagePanel.getMessageField().requestFocus();
            getMainActivity().showKeyboard(messagePanel.getMessageField());
        }
    }

    private void hideMessagePanel() {
        messagePanel.setVisibility(View.GONE);
        messagePanel.hidePopupWindows();
        hidePopupWindows();
        messagePanel.getHeightChangeListener().onChangedHeight(0);
        toggleMessagePanelItem.setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_create));
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        toggleMessagePanelItem = menu
                .add(R.string.reply)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_create))
                .setOnMenuItemClickListener(menuItem -> {
                    toggleMessagePanel();
                    return false;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        refreshMenuItem = menu
                .add(R.string.refresh)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_refresh))
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.reload();
                    return false;
                });

        copyLinkMenuItem = menu
                .add(R.string.copy_link)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.copyLink();
                    return false;
                });
        addSearchOnPageItem(menu);
        searchInThemeMenuItem = menu
                .add(R.string.search_in_theme)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.openSearch();
                    return false;
                });
        searchPostsMenuItem = menu
                .add(R.string.search_my_posts)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.openSearchMyPosts();
                    return false;
                });

        deleteFavoritesMenuItem = menu
                .add(R.string.delete_from_favorites)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.onClickDeleteInFav();
                    return false;
                });
        addFavoritesMenuItem = menu
                .add(R.string.add_to_favorites)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.onClickAddInFav();
                    return false;
                });
        openForumMenuItem = menu
                .add(R.string.open_theme_forum)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.openForum();
                    return false;
                });

        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            boolean pageNotNull = presenter.isPageLoaded();

            toggleMessagePanelItem.setEnabled(true);
            refreshMenuItem.setEnabled(true);
            copyLinkMenuItem.setEnabled(pageNotNull);
            searchInThemeMenuItem.setEnabled(pageNotNull);
            searchPostsMenuItem.setEnabled(pageNotNull);
            searchOnPageMenuItem.setEnabled(pageNotNull);
            deleteFavoritesMenuItem.setEnabled(pageNotNull);
            addFavoritesMenuItem.setEnabled(pageNotNull);
            if (pageNotNull) {
                if (presenter.isInFavorites()) {
                    deleteFavoritesMenuItem.setVisible(true);
                    addFavoritesMenuItem.setVisible(false);
                } else {
                    deleteFavoritesMenuItem.setVisible(false);
                    addFavoritesMenuItem.setVisible(true);
                }
            }
            openForumMenuItem.setEnabled(pageNotNull);
        } else {
            toggleMessagePanelItem.setEnabled(false);
            refreshMenuItem.setEnabled(true);
            copyLinkMenuItem.setEnabled(false);
            searchInThemeMenuItem.setEnabled(false);
            searchPostsMenuItem.setEnabled(false);
            searchOnPageMenuItem.setEnabled(false);
            deleteFavoritesMenuItem.setEnabled(false);
            addFavoritesMenuItem.setEnabled(false);
            deleteFavoritesMenuItem.setVisible(false);
            addFavoritesMenuItem.setVisible(false);
            openForumMenuItem.setEnabled(false);
        }
        if (!ClientHelper.getAuthState()) {
            toggleMessagePanelItem.setVisible(false);
            deleteFavoritesMenuItem.setVisible(false);
            addFavoritesMenuItem.setVisible(false);
            searchPostsMenuItem.setEnabled(false);
            hideMessagePanel();
        }
    }

    private void addSearchOnPageItem(Menu menu) {
        toolbar.inflateMenu(R.menu.theme_search_menu);
        searchOnPageMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchOnPageMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                toggleMessagePanelItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                toggleMessagePanelItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
                return true;
            }
        });
        SearchView searchView = (SearchView) searchOnPageMenuItem.getActionView();
        searchView.setTag(searchViewTag);

        searchView.setOnSearchClickListener(v -> {
            if (searchView.getTag().equals(searchViewTag)) {
                ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
                if (searchClose != null)
                    ((ViewGroup) searchClose.getParent()).removeView(searchClose);

                ViewGroup.LayoutParams navButtonsParams = new ViewGroup.LayoutParams(App.px48, App.px48);
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true);

                AppCompatImageButton btnNext = new AppCompatImageButton(searchView.getContext());
                btnNext.setImageDrawable(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_search_next));
                btnNext.setBackgroundResource(outValue.resourceId);

                AppCompatImageButton btnPrev = new AppCompatImageButton(searchView.getContext());
                btnPrev.setImageDrawable(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_search_prev));
                btnPrev.setBackgroundResource(outValue.resourceId);

                ((LinearLayout) searchView.getChildAt(0)).addView(btnPrev, navButtonsParams);
                ((LinearLayout) searchView.getChildAt(0)).addView(btnNext, navButtonsParams);

                btnNext.setOnClickListener(v1 -> findNext(true));
                btnPrev.setOnClickListener(v1 -> findNext(false));
                searchViewTag++;
            }
        });

        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));
        }

        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                findText(newText);
                return false;
            }
        });
    }

    @Override
    public void showAddInFavDialog(@NotNull ThemePage page) {
        FavoritesHelper.addWithDialog(getContext(), aBoolean -> {
            Toast.makeText(App.getContext(), aBoolean ? getString(R.string.favorites_added) : getString(R.string.error), Toast.LENGTH_SHORT).show();
            page.setInFavorite(aBoolean);
            refreshToolbarMenuItems(true);
        }, page.getId());
    }

    @Override
    public void showDeleteInFavDialog(@NotNull ThemePage page) {
        if (page.getFavId() == 0) {
            Toast.makeText(App.getContext(), R.string.fav_delete_error_id_not_found, Toast.LENGTH_SHORT).show();
        }
        FavoritesHelper.deleteWithDialog(getContext(), aBoolean -> {
            Toast.makeText(App.getContext(), getString(aBoolean ? R.string.favorite_theme_deleted : R.string.error), Toast.LENGTH_SHORT).show();
            page.setInFavorite(!aBoolean);
            refreshToolbarMenuItems(true);
        }, page.getFavId());
    }


    /*
    *
    * EDIT POST FUNCTIONS
    *
    * */

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public AttachmentsPopup getAttachmentsPopup() {
        return attachmentsPopup;
    }


    public void onSendPostCompleted(ThemePage themePage) throws Exception {
        messagePanel.clearAttachments();
        messagePanel.clearMessage();
        onLoadData(themePage);
    }

    public void onEditPostCompleted(ThemePage themePage) throws Exception {
        onLoadData(themePage);
    }

    private void sendMessage() {
        hidePopupWindows();
        presenter.sendMessage(messagePanel.getMessage(), messagePanel.getAttachments());
    }

    @Override
    public void onMessageSent() {
        messagePanel.clearAttachments();
        messagePanel.clearMessage();
        if (Preferences.Main.isEditorDefaultHidden(getContext())) {
            hideMessagePanel();
        }
    }

    @Override
    public void setMessageRefreshing(boolean isRefreshing) {
        messagePanel.setProgressState(isRefreshing);
    }

    public void tryPickFile() {
        App.get().checkStoragePermission(() -> startActivityForResult(FilePickHelper.pickFile(false), REQUEST_PICK_FILE), App.getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            uploadFiles(FilePickHelper.onActivityResult(getContext(), data));
        }
    }

    public void uploadFiles(List<RequestFile> files) {
        List<AttachmentItem> pending = attachmentsPopup.preUploadFiles(files);
        subscribe(RxApi.EditPost().uploadFiles(0, files, pending), items -> attachmentsPopup.onUploadFiles(items), new ArrayList<>(), null);
    }

    public void removeFiles() {
        attachmentsPopup.preDeleteFiles();
        List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
        subscribe(RxApi.EditPost().deleteFiles(0, selectedFiles), item -> attachmentsPopup.onDeleteFiles(selectedFiles), selectedFiles, null);
    }



    /*
    *
    * Post functions
    *
    * */

    @Override
    public void showNoteCreate(@NotNull String title, @NotNull String url) {
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
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


    @Override
    public void insertText(@NotNull String text) {
        messagePanel.insertText(text);
        showMessagePanel(true);
    }

    @Override
    public void editPost(@NotNull IBaseForumPost post) {
        presenter.openEditPostForm(post.getId());
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
}
