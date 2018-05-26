package forpdateam.ru.forpda.ui.views.drawers;

import android.support.annotation.DrawableRes;

import forpdateam.ru.forpda.presentation.Screen;

public class MenuItem {
    private int titleRes;
    private int iconRes;
    private int notifyCount = 0;
    private Screen screen;
    private int action;
    private boolean active = false;

    public MenuItem(int title, @DrawableRes int iconRes, Screen screen) {
        this.titleRes = title;
        this.iconRes = iconRes;
        this.screen = screen;
    }

    public MenuItem(int title, @DrawableRes int iconRes, int action) {
        this.titleRes = title;
        this.iconRes = iconRes;
        this.action = action;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getNotifyCount() {
        return notifyCount;
    }

    public int getAction() {
        return action;
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
