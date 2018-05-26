package forpdateam.ru.forpda.ui.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.LocaleHelper;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.common.webview.WebViewsProvider;
import forpdateam.ru.forpda.notifications.NotificationsService;
import forpdateam.ru.forpda.presentation.Screen;
import forpdateam.ru.forpda.presentation.TabRouter;
import forpdateam.ru.forpda.ui.activities.updatechecker.SimpleUpdateChecker;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.navigation.TabNavigator;
import forpdateam.ru.forpda.ui.views.KeyboardUtil;
import forpdateam.ru.forpda.ui.views.drawers.DrawerHeader;
import forpdateam.ru.forpda.ui.views.drawers.MenuDrawer;
import forpdateam.ru.forpda.ui.views.drawers.TabsDrawer;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public final static String LOG_TAG = MainActivity.class.getSimpleName();
    public final static String DEF_TITLE = "ForPDA";
    public final static String ARG_CHECK_WEBVIEW = "CHECK_WEBVIEW";
    private WebViewsProvider webViewsProvider;
    private MenuDrawer menuDrawer;
    private TabsDrawer tabsDrawer;
    private DrawerHeader drawerHeader;
    private final View.OnClickListener toggleListener = view -> menuDrawer.toggleMenu();
    private final View.OnClickListener removeTabListener = view -> backHandler(true);
    private List<SimpleTooltip> tooltips = new ArrayList<>();
    private boolean currentThemeIsDark = App.get().isDarkTheme();
    private boolean checkWebView = true;

    private TabNavigator tabNavigator = new TabNavigator(this, R.id.fragments_container);


    public View.OnClickListener getToggleListener() {
        return toggleListener;
    }

    public View.OnClickListener getRemoveTabListener() {
        return removeTabListener;
    }

    public MainActivity() {
        webViewsProvider = new WebViewsProvider();
    }

    public TabNavigator getTabNavigator() {
        return tabNavigator;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (EmptyActivity.empty(App.get().getPreferences().getString("auth.user.nick", ""))) {
            startActivity(new Intent(this, EmptyActivity.class));
            finish();
            return;
        }
        Intent intent = getIntent();
        if (intent != null) {
            checkWebView = intent.getBooleanExtra(ARG_CHECK_WEBVIEW, checkWebView);
        }
        if (checkWebView) {
            Disposable disposable = Observable
                    .fromCallable(() -> App.get().isWebViewFound(this))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aBoolean -> {
                        if (!aBoolean) {
                            startActivity(new Intent(App.getContext(), WebVewNotFoundActivity.class));
                            finish();
                        }
                    });
        }

        currentThemeIsDark = App.get().isDarkTheme();
        setTheme(currentThemeIsDark ? R.style.DarkAppTheme_NoActionBar : R.style.LightAppTheme_NoActionBar);
        setContentView(R.layout.activity_main);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        /*
         * Т.к. 2 вьюхи, делаю цвет в 2 раза прозрачнее, чтобы компенсировать это.
         * P.S. Чем больше вьюх в DrawerLayout находятся до NavigationView, тем сильнее будет затенение
         * P.S.S. Первая вьюха - контейнер фрагментов, вторая - view_for_measure
         * */
        drawerLayout.setScrimColor(0x4C000000);
        menuDrawer = new MenuDrawer(this, drawerLayout, tabNavigator);
        tabsDrawer = new TabsDrawer(this, drawerLayout, tabNavigator);
        drawerHeader = new DrawerHeader(this, drawerLayout, v -> {
            menuDrawer.closeMenu();
            tabsDrawer.closeTabs();
        });
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerView.getId() == R.id.menu_drawer) {
                    if (App.get().getPreferences().getBoolean("drawers.tooltip.link_open", true)) {
                        SimpleTooltip tooltip = new SimpleTooltip.Builder(MainActivity.this)
                                .anchorView(drawerView.findViewById(R.id.drawer_header_open_link))
                                .text(R.string.tooltip_link)
                                .gravity(Gravity.BOTTOM)
                                .animated(false)
                                .modal(true)
                                .transparentOverlay(false)
                                .backgroundColor(Color.BLACK)
                                .textColor(Color.WHITE)
                                .padding((float) App.px16)
                                .onDismissListener(simpleTooltip -> tooltips.remove(simpleTooltip))
                                .build();
                        tooltip.show();
                        tooltips.add(tooltip);
                        App.get().getPreferences().edit().putBoolean("drawers.tooltip.link_open", false).apply();
                    }

                }

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (drawerView.getId() == R.id.menu_drawer) {
                    if (App.get().getPreferences().getBoolean("drawers.tooltip.tabs_drawer", true)) {
                        SimpleTooltip tooltip = new SimpleTooltip.Builder(MainActivity.this)
                                .anchorView(tabsDrawer.getTabDrawer())
                                .text(R.string.tooltip_tabs)
                                .gravity(Gravity.START)
                                .animated(false)
                                .modal(true)
                                .transparentOverlay(false)
                                .backgroundColor(Color.BLACK)
                                .textColor(Color.WHITE)
                                .padding((float) App.px16)
                                .onDismissListener(simpleTooltip -> tooltips.remove(simpleTooltip))
                                .build();

                        tooltip.show();
                        tooltips.add(tooltip);
                        App.get().getPreferences().edit().putBoolean("drawers.tooltip.tabs_drawer", false).apply();
                    }
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });


        KeyboardUtil keyboardUtil = new KeyboardUtil(this, findViewById(R.id.fragments_container));
        keyboardUtil.enable();

        final View viewDiff = findViewById(R.id.view_for_measure);
        viewDiff.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> measureView(v));
        //viewDiff.post(() -> measureView(viewDiff));

        if (Preferences.Notifications.Update.isEnabled(getApplicationContext())) {
            new SimpleUpdateChecker().checkFromGitHub(this);
        }
        checkIntent(getIntent());
        TabRouter router = App.get().Di().getRouter();
        router.navigateTo(new Screen.ArticleList());
    }

    private void measureView(View v) {
        Log.d(LOG_TAG, "Calc SOOOKA " + ((int) (((View) v.getParent()).getTop())) + " : " + v.getTop() + " : " + v.getRootView().getTop() + " : " + v.getRootView().getHeight() + " : " + findViewById(R.id.fragments_container).getHeight());

        int lastHeight = App.getStatusBarHeight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            App.setStatusBarHeight((int) (((View) v.getParent()).getTop() + v.getTop()));
            App.setNavigationBarHeight(v.getRootView().getHeight() - findViewById(R.id.fragments_container).getHeight());
        }


        if (lastHeight != App.getStatusBarHeight()) {
            Log.d(LOG_TAG, "Calc SB: " + App.getStatusBarHeight() + ", NB: " + App.getNavigationBarHeight());
            App.get().getStatusBarSizeObservables().notifyObservers();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotificationsService.startAndCheck();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "onNewIntent " + intent.toString());
        checkIntent(intent);
    }

    void checkIntent(Intent intent) {
        //todo fix it
        /*if (intent == null || intent.getData() == null) {
            if (tabManager.isEmpty()) {
                drawers.firstSelect();
            }
            return;
        }

        new Handler().post(() -> {
            Log.d(LOG_TAG, "Handler.post checkIntent: " + intent);
            boolean handled = IntentHandler.handle(intent.getData().toString());
            if (!handled || tabManager.isEmpty()) {
                drawers.firstSelect();
            }
            setIntent(null);
        });*/
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed");
        if (!tooltips.isEmpty()) {
            tooltips.get(tooltips.size() - 1).dismiss();
            return;
        }
        if (menuDrawer.isMenuOpen()) {
            menuDrawer.closeMenu();
            return;
        }
        if (tabsDrawer.isTabsOpen()) {
            tabsDrawer.closeTabs();
            return;
        }
        backHandler(false);
    }

    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager iim = ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE));
            if (iim != null) {
                iim.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    public void showKeyboard(View view) {
        if (getCurrentFocus() != null) {
            InputMethodManager iim = ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE));
            if (iim != null) {
                iim.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    public void backHandler(boolean fromToolbar) {
        TabFragment active = tabNavigator.getCurrentFragment();
        if (active == null) {
            App.get().Di().getRouter().exit();
            return;
        }
        if (fromToolbar || !active.onBackPressed()) {
            hideKeyboard();
            tabNavigator.close(active.getTag());
        }
    }

    public WebViewsProvider getWebViewsProvider() {
        return webViewsProvider;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Log.d(LOG_TAG, "onResumeFragments");
        App.get().Di().getNavigatorHolder().setNavigator(tabNavigator);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    private String lang = null;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        if (lang == null) {
            lang = LocaleHelper.getLanguage(this);
        }
        if (!LocaleHelper.getLanguage(this).equals(lang)) {
            Context newContext = LocaleHelper.onAttach(this);
            new AlertDialog.Builder(this)
                    .setMessage(newContext.getString(R.string.lang_changed))
                    .setPositiveButton(newContext.getString(R.string.ok), (dialog, which) -> {
                        MainActivity.restartApplication(MainActivity.this);
                    })
                    .setNegativeButton(newContext.getString(R.string.cancel), null)
                    .show();
        }
        if (currentThemeIsDark != App.get().isDarkTheme()) {
            recreate();
        }
    }

    public static void restartApplication(Activity activity) {
        Intent mStartActivity = new Intent(activity, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(activity, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        activity.finish();
        System.exit(0);
    }

    @Override
    protected void onPause() {
        App.get().Di().getNavigatorHolder().removeNavigator();
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();

        if (menuDrawer != null) {
            menuDrawer.destroy();
        }
        if (tabsDrawer != null) {
            tabsDrawer.destroy();
        }
        if (drawerHeader != null) {
            drawerHeader.destroy();
        }
        if (webViewsProvider != null) {
            webViewsProvider.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        App.get().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
