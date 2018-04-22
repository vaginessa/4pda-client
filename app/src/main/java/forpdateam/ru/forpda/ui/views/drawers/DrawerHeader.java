package forpdateam.ru.forpda.ui.views.drawers;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel;
import forpdateam.ru.forpda.model.NetworkStateProvider;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.activities.MainActivity;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.profile.ProfileFragment;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by radiationx on 16.03.17.
 */

public class DrawerHeader {
    private ImageView avatar;
    private TextView nick;
    private View headerLayout;
    private ImageButton openLinkButton;
    private MainActivity activity;

    private CompositeDisposable disposables = new CompositeDisposable();

    private NetworkStateProvider networkState = App.get().Di().getNetworkState();

    private Consumer<Boolean> networkObserver = state -> {
        if (state) {
            state(ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN);
        }
    };

    private View.OnClickListener headerClickListener = v -> {
        TabFragment tabFragment = null;
        for (TabFragment fragment : TabManager.get().getFragments()) {
            if (fragment.getClass().getSimpleName().equals(ProfileFragment.class.getSimpleName()) && fragment.getConfiguration().isMenu()) {
                tabFragment = fragment;
                break;
            }
        }
        if (tabFragment == null) {
            tabFragment = new TabFragment.Builder<>(ProfileFragment.class)
                    .setIsMenu()
                    .build();
            TabManager.get().add(tabFragment);
        } else {
            TabManager.get().select(tabFragment);
        }
        /*TabFragment fragment = TabManager.get().get(TabManager.get().getTagContainClass(ProfileFragment.class));
        if (fragment == null | (fragment != null && fragment.getConfiguration().isMenu())) {
            TabManager.get().add(new TabFragment.Builder<>(ProfileFragment.class).setIsMenu().build());
        } else {
            TabManager.get().select(fragment);
        }*/
        activity.getDrawers().closeMenu();
        activity.getDrawers().closeTabs();
    };

    private Observer loginObserver = (observable, o) -> {
        if (o == null) o = false;
        state((boolean) o);
    };

    private void addToDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    public void destroy() {
        activity = null;
        if (!disposables.isDisposed())
            disposables.dispose();
        ClientHelper.get().removeLoginObserver(loginObserver);
    }

    public DrawerHeader(MainActivity activity, DrawerLayout drawerLayout) {
        this.activity = activity;
        headerLayout = drawerLayout.findViewById(R.id.drawer_header_container);
        avatar = (ImageView) headerLayout.findViewById(R.id.drawer_header_avatar);
        nick = (TextView) headerLayout.findViewById(R.id.drawer_header_nick);
        openLinkButton = (ImageButton) headerLayout.findViewById(R.id.drawer_header_open_link);
        openLinkButton.setOnClickListener(v -> {
            activity.getDrawers().closeMenu();
            activity.getDrawers().closeTabs();
            String url;
            url = Utils.readFromClipboard();
            if (url == null) url = "";
            final FrameLayout frameLayout = new FrameLayout(activity);
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            frameLayout.setPadding(App.px24, 0, App.px24, 0);
            final EditText linkField = new EditText(activity);
            frameLayout.addView(linkField);
            linkField.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linkField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            linkField.setText(url);
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.follow_link)
                    .setView(frameLayout)
                    .setPositiveButton(R.string.follow, (dialog, which) -> IntentHandler.handle(linkField.getText().toString()))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
        ClientHelper.get().addLoginObserver(loginObserver);
        addToDisposable(
                networkState
                        .observeState()
                        .subscribe(networkObserver)
        );
        state(ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN);
    }

    private void state(boolean b) {
        if (b) {
            headerLayout.setOnClickListener(headerClickListener);
            nick.setText("");
            load();
        } else {
            headerLayout.setOnClickListener(null);
            ImageLoader.getInstance().displayImage("assets://av.png", avatar);
            nick.setText(R.string.auth_guest);
        }
    }

    private void load() {
        String url = "https://4pda.ru/forum/index.php?showuser=".concat(Integer.toString(ClientHelper.getUserId() == 0 ? 2556269 : ClientHelper.getUserId()));
        Disposable disposable = App.get().Di().getProfileRepository()
                .loadProfile(url)
                .subscribe(this::onLoad, Throwable::printStackTrace);
    }

    private void onLoad(ProfileModel profileModel) {
        ImageLoader.getInstance().displayImage(profileModel.getAvatar(), avatar);
        nick.setText(profileModel.getNick());
        App.get().getPreferences().edit().putString("auth.user.nick", profileModel.getNick()).apply();
    }

    public void setStatusBarHeight(int height) {
        headerLayout.setPadding(0, height, 0, 0);

    }
}
