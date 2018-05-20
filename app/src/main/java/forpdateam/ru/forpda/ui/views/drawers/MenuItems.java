package forpdateam.ru.forpda.ui.views.drawers;

import android.support.annotation.DrawableRes;

import java.util.ArrayList;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.presentation.Screen;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.auth.AuthFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.brands.BrandsFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.ui.fragments.forum.ForumFragment;
import forpdateam.ru.forpda.ui.fragments.history.HistoryFragment;
import forpdateam.ru.forpda.ui.fragments.mentions.MentionsFragment;
import forpdateam.ru.forpda.ui.fragments.news.main.NewsMainFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesFragment;
import forpdateam.ru.forpda.ui.fragments.other.ForumRulesFragment;
import forpdateam.ru.forpda.ui.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment;

/**
 * Created by radiationx on 02.05.17.
 */

public class MenuItems {
    public final static int ACTION_APP_SETTINGS = 0;
    private ArrayList<MenuItem> createdMenuItems = new ArrayList<>();

    public MenuItems() {
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_auth), R.drawable.ic_person_add, new Screen.Auth()));
        //if (Objects.equals(BuildConfig.FLAVOR, "dev"))
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_news), R.drawable.ic_newspaper, new Screen.ArticleList()));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_favorite), R.drawable.ic_star, new Screen.Favorites()));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_contacts), R.drawable.ic_contacts, new Screen.QmsContacts()));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_mentions), R.drawable.ic_notifications, new Screen.Mentions()));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_devdb), R.drawable.ic_devices_other, new Screen.DevDbBrands()));
        //createdMenuItems.add(new MenuItem("DevDB dev", R.drawable.ic_devices_other, DeviceFragment.class));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_forum), R.drawable.ic_forum, new Screen.Forum()));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_search), R.drawable.ic_search, new Screen.Search()));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_history), R.drawable.ic_history, new Screen.History()));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_notes), R.drawable.ic_bookmark, new Screen.Notes()));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.fragment_title_forum_rules), R.drawable.ic_book_open, new Screen.ForumRules()));
        createdMenuItems.add(new MenuItem(App.get().getString(R.string.activity_title_settings), R.drawable.ic_settings, new Screen.Settings()));
    }

    public ArrayList<MenuItem> getCreatedMenuItems() {
        return createdMenuItems;
    }

    public class MenuItem {
        private String title;
        private int iconRes;
        private int notifyCount = 0;
        private String attachedTabTag = "";
        private Screen screen;
        private int action;
        private boolean active = false;

        public MenuItem(String title, @DrawableRes int iconRes, Screen screen) {
            this.title = title;
            this.iconRes = iconRes;
            this.screen = screen;
        }

        public MenuItem(String title, @DrawableRes int iconRes, int action) {
            this.title = title;
            this.iconRes = iconRes;
            this.action = action;
        }

        public String getTitle() {
            return title;
        }

        public int getIconRes() {
            return iconRes;
        }

        public int getNotifyCount() {
            return notifyCount;
        }

        public String getAttachedTabTag() {
            return attachedTabTag;
        }

        public int getAction() {
            return action;
        }

        public void setAttachedTabTag(String attachedTabTag) {
            this.attachedTabTag = attachedTabTag;
        }

        public void setNotifyCount(int notifyCount) {
            this.notifyCount = notifyCount;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public Screen getScreen() {
            return screen;
        }
    }
}
