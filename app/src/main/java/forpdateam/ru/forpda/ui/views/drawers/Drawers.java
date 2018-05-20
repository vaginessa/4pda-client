package forpdateam.ru.forpda.ui.views.drawers;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.presentation.Screen;
import forpdateam.ru.forpda.presentation.TabRouter;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.activities.MainActivity;
import forpdateam.ru.forpda.ui.activities.SettingsActivity;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.auth.AuthFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.ui.fragments.mentions.MentionsFragment;
import forpdateam.ru.forpda.ui.fragments.profile.ProfileFragment;
import forpdateam.ru.forpda.ui.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.drawers.adapters.MenuAdapter;
import forpdateam.ru.forpda.ui.views.drawers.adapters.TabAdapter;

/**
 * Created by radiationx on 02.05.17.
 */

public class Drawers {
    private final static String LOG_TAG = Drawers.class.getSimpleName();
    private MainActivity activity;
    private DrawerLayout drawerLayout;

    private NavigationView menuDrawer;
    private RecyclerView menuListView;
    private TextView forbiddenError;
    private LinearLayoutManager menuListLayoutManager;
    private MenuAdapter menuAdapter;
    private MenuItems allMenuItems = new MenuItems();
    private ArrayList<MenuItems.MenuItem> menuItems = new ArrayList<>();
    private MenuItems.MenuItem lastActive;

    private NavigationView tabDrawer;
    private RecyclerView tabListView;
    private LinearLayoutManager tabListLayoutManager;
    private TabAdapter tabAdapter;
    private Button tabCloseAllButton;

    boolean isFirstSelected = false;

    private TabRouter router = App.get().Di().getRouter();

    private Observer loginObserver = (observable, o) -> {
        if (o == null) o = false;
        menuItems.clear();
        fillMenuItems();
        menuAdapter.notifyDataSetChanged();
        //todo fix this
        /*if ((boolean) o && TabManager.get().getSize() <= 1) {
            //select(findByClassName(NewsTimelineFragment.class.getSimpleName()));
            selectMenuItem(findMenuItem(Screen.Favorites.class));
        }*/
        if (!(boolean) o) {
            ClientHelper.setQmsCount(0);
            ClientHelper.setFavoritesCount(0);
            ClientHelper.setMentionsCount(0);
            ClientHelper.get().notifyCountsChanged();
            App.get().getPreferences().edit().remove("menu_drawer_last").apply();
        }
    };

    private Observer countsObserver = (observable1, o) -> {
        MenuItems.MenuItem item = findMenuItem(Screen.QmsContacts.class);
        if (item != null) {
            item.setNotifyCount(ClientHelper.getQmsCount());
        }
        item = findMenuItem(Screen.Mentions.class);
        if (item != null) {
            item.setNotifyCount(ClientHelper.getMentionsCount());
        }
        item = findMenuItem(Screen.Favorites.class);
        if (item != null) {
            item.setNotifyCount(ClientHelper.getFavoritesCount());
        }
        menuAdapter.notifyDataSetChanged();
    };

    private Observer preferenceObserver = (observable1, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Main.IS_TABS_BOTTOM: {
                updateTabGravity();
                break;
            }
        }
    };

    private Observer statusBarSizeObserver = (observable1, o) -> {
        setStatusBarHeight(App.getStatusBarHeight());
    };
    private Observer forbiddenObserver = (o, arg) -> {
        if (activity == null)
            return;
        activity.runOnUiThread(() -> {
            if (forbiddenError != null) {
                boolean isForbidden = (boolean) arg;
                forbiddenError.setVisibility(isForbidden ? View.VISIBLE : View.GONE);
            }
        });
    };

    public Drawers(MainActivity activity, DrawerLayout drawerLayout) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        menuDrawer = (NavigationView) activity.findViewById(R.id.menu_drawer);
        tabDrawer = (NavigationView) activity.findViewById(R.id.tab_drawer);

        menuListView = (RecyclerView) activity.findViewById(R.id.menu_list);
        tabListView = (RecyclerView) activity.findViewById(R.id.tab_list);

        forbiddenError = (TextView) activity.findViewById(R.id.forbidden_error);
        tabCloseAllButton = (Button) activity.findViewById(R.id.tab_close_all);

        menuListLayoutManager = new LinearLayoutManager(activity);
        tabListLayoutManager = new LinearLayoutManager(activity);
        tabListLayoutManager.setStackFromEnd(Preferences.Main.isTabsBottom(activity));
        menuListView.setLayoutManager(menuListLayoutManager);
        tabListView.setLayoutManager(tabListLayoutManager);


        menuAdapter = new MenuAdapter();

        tabAdapter = new TabAdapter();

        menuListView.setAdapter(menuAdapter);
        tabListView.setAdapter(tabAdapter);

        menuAdapter.setItems(menuItems);

        tabCloseAllButton.setOnClickListener(v -> closeAllTabs());
        App.get().addPreferenceChangeObserver(preferenceObserver);
        App.get().addStatusBarSizeObserver(statusBarSizeObserver);
        App.get().subscribeForbidden(forbiddenObserver);
    }

    public NavigationView getMenuDrawer() {
        return menuDrawer;
    }

    public NavigationView getTabDrawer() {
        return tabDrawer;
    }

    private Bundle savedInstanceState;

    public void init(Bundle savedInstanceState) {
        initMenu(savedInstanceState);
        //initTabs(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        //firstSelect(savedInstanceState);
    }

    public void firstSelect() {
        if (isFirstSelected)
            return;
        isFirstSelected = true;
        MenuItems.MenuItem item;
        if (ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN) {
            item = findMenuItem(Screen.Favorites.class);
        } else {
            item = findMenuItem(Screen.Auth.class);
        }
        selectMenuItem(item);

    }

    public void destroy() {
        App.get().removePreferenceChangeObserver(preferenceObserver);
        App.get().removeStatusBarSizeObserver(statusBarSizeObserver);
        App.get().unSubscribeForbidden(forbiddenObserver);
        ClientHelper.get().removeLoginObserver(loginObserver);
        ClientHelper.get().removeCountsObserver(countsObserver);
        //menuAdapter.clear();
        //tabAdapter.clear();
    }

    public void setStatusBarHeight(int height) {
        menuDrawer.setPadding(0, height, 0, 0);
        tabDrawer.setPadding(0, height, 0, 0);
    }

    private void initMenu(Bundle savedInstanceState) {
        fillMenuItems();
        ClientHelper.get().addLoginObserver(loginObserver);
        ClientHelper.get().addCountsObserver(countsObserver);
        menuAdapter.setItemClickListener(new BaseAdapter.OnItemClickListener<MenuItems.MenuItem>() {
            @Override
            public void onItemClick(MenuItems.MenuItem item) {
                selectMenuItem(item);
                closeMenu();
            }

            @Override
            public boolean onItemLongClick(MenuItems.MenuItem item) {
                return false;
            }
        });
    }

    private void fillMenuItems() {
        menuItems.clear();
        for (MenuItems.MenuItem item : allMenuItems.getCreatedMenuItems()) {
            Class screen = item.getScreen().getClass();
            if (screen == Screen.Auth.class && ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN) {
                continue;
            } else if (ClientHelper.getAuthState() != ClientHelper.AUTH_STATE_LOGIN) {
                if (screen == Screen.Profile.class || screen == Screen.QmsContacts.class || screen == Screen.Favorites.class || screen == Screen.Mentions.class) {
                    continue;
                }
            }
            menuItems.add(item);
        }
    }

    private void selectMenuItem(MenuItems.MenuItem item) {
        Log.d(LOG_TAG, "selectMenuItem " + item);
        if (item == null) return;
        if (item.getScreen().getClass() == Screen.Settings.class) {
            activity.startActivity(new Intent(activity, SettingsActivity.class));
        } else {
            router.navigateTo(item.getScreen());
            if (lastActive != null)
                lastActive.setActive(false);
            item.setActive(true);
            lastActive = item;
            menuAdapter.notifyDataSetChanged();
        }
    }

    private MenuItems.MenuItem findMenuItem(Class<? extends Screen> classObject) {
        for (MenuItems.MenuItem item : menuItems) {
            if (item.getScreen().getClass() == classObject)
                return item;
        }
        return null;
    }

    public void openMenu() {
        drawerLayout.openDrawer(menuDrawer);
    }

    public boolean isMenuOpen() {
        return drawerLayout.isDrawerOpen(menuDrawer);
    }

    public void closeMenu() {
        drawerLayout.closeDrawer(menuDrawer);
    }

    public void toggleMenu() {
        if (drawerLayout.isDrawerOpen(menuDrawer)) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    private void initTabs(Bundle savedInstanceState) {
        //todo fix it
        tabAdapter.setItemClickListener(new BaseAdapter.OnItemClickListener<TabFragment>() {
            @Override
            public void onItemClick(TabFragment item) {
                //TabManager.get().select(item);
                closeTabs();
            }

            @Override
            public boolean onItemLongClick(TabFragment item) {
                return false;
            }
        });

        tabAdapter.setCloseClickListener(new BaseAdapter.OnItemClickListener<TabFragment>() {
            @Override
            public void onItemClick(TabFragment item) {
                /*TabManager.get().remove(item);
                if (TabManager.get().getSize() < 1) {
                    activity.finish();
                }*/
            }

            @Override
            public boolean onItemLongClick(TabFragment item) {
                return false;
            }
        });
        //TabManager.get().loadState(savedInstanceState);
        //TabManager.get().updateFragmentList();
    }

    public void notifyTabsChanged() {
        tabAdapter.notifyDataSetChanged();
    }

    public void openTabs() {
        drawerLayout.openDrawer(tabDrawer);
    }

    public void closeTabs() {
        drawerLayout.closeDrawer(tabDrawer);
    }

    public boolean isTabsOpen() {
        return drawerLayout.isDrawerOpen(tabDrawer);
    }

    public void toggleTabs() {
        if (drawerLayout.isDrawerOpen(tabDrawer)) {
            closeTabs();
        } else {
            openTabs();
        }
    }

    public void closeAllTabs() {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.ask_close_other_tabs)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    closeTabs();
                    router.exitOthers();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void updateTabGravity() {
        tabListLayoutManager.setStackFromEnd(Preferences.Main.isTabsBottom(activity));
    }
}
