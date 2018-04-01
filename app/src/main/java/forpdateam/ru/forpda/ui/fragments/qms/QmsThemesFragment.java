package forpdateam.ru.forpda.ui.fragments.qms;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme;
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes;
import forpdateam.ru.forpda.presentation.qms.themes.QmsThemesPresenter;
import forpdateam.ru.forpda.presentation.qms.themes.QmsThemesView;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsThemesAdapter;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsThemesFragment extends RecyclerFragment implements QmsThemesAdapter.OnItemClickListener<QmsTheme>, QmsThemesView {
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    private MenuItem blackListMenuItem;
    private MenuItem noteMenuItem;
    private QmsThemesAdapter adapter;
    private DynamicDialogMenu<QmsThemesFragment, QmsTheme> dialogMenu = new DynamicDialogMenu<>();
    private Observer notification = (observable, o) -> {
        if (o == null) return;
        TabNotification event = (TabNotification) o;
        //runInUiThread(() -> handleEvent(event));
    };

    @InjectPresenter
    QmsThemesPresenter presenter;

    @ProvidePresenter
    QmsThemesPresenter providePresenter() {
        return new QmsThemesPresenter(App.get().Di().getQmsRepository());
    }

    public QmsThemesFragment() {
        //configuration.setUseCache(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_dialogs));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            presenter.setThemesId(getArguments().getInt(USER_ID_ARG));
            presenter.setAvatarUrl(getArguments().getString(USER_AVATAR_ARG));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        contentController.setFirstLoad(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFabBehavior();

        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fab.setImageDrawable(App.getVecDrawable(getContext(), R.drawable.ic_fab_create));
        fab.setOnClickListener(view1 -> presenter.openChat());
        fab.setVisibility(View.VISIBLE);

        dialogMenu.addItem(getString(R.string.delete), (context, data) -> presenter.deleteTheme(data.getId()));
        dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> presenter.createThemeNote(data));

        adapter = new QmsThemesAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        QmsHelper.get().subscribeQms(notification);
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        blackListMenuItem = menu
                .add(R.string.add_to_blacklist)
                .setOnMenuItemClickListener(item -> {
                    presenter.blockUser();
                    return false;
                });
        noteMenuItem = menu
                .add(R.string.create_note)
                .setOnMenuItemClickListener(item -> {
                    presenter.createNote();
                    return true;
                });
        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            blackListMenuItem.setEnabled(true);
            noteMenuItem.setEnabled(true);
        } else {
            blackListMenuItem.setEnabled(false);
            noteMenuItem.setEnabled(false);
        }
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        super.setRefreshing(isRefreshing);
        refreshToolbarMenuItems(!isRefreshing);
    }

    @Override
    public void showThemes(@NotNull QmsThemes data) {
        recyclerView.scrollToPosition(0);

        setTabTitle(String.format(getString(R.string.dialogs_Nick), data.getNick()));
        setTitle(data.getNick());
        if (data.getThemes().isEmpty() && data.getNick() != null) {
            presenter.openChat();
        }

        adapter.addAll(data.getThemes());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showAvatar(@NotNull String avatarUrl) {
        ImageLoader.getInstance().displayImage(avatarUrl, toolbarImageView);
        toolbarImageView.setVisibility(View.VISIBLE);
        toolbarImageView.setOnClickListener(view1 -> presenter.openProfile(presenter.getThemesId()));
        toolbarImageView.setContentDescription(App.get().getString(R.string.user_avatar));
    }

    @Override
    public void showCreateNote(@NotNull String nick, @NotNull String url) {
        String title = String.format(getString(R.string.dialogs_Nick), nick);
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
    }

    @Override
    public void showCreateNote(@NotNull String name, @NotNull String nick, @NotNull String url) {
        String title = String.format(getString(R.string.dialog_Title_Nick), name, nick);
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
    }

    @Override
    public void onBlockUser(boolean res) {
        Toast.makeText(getContext(), R.string.user_added_to_blacklist, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showItemDialogMenu(@NotNull QmsTheme item) {
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), QmsThemesFragment.this, item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QmsHelper.get().unSubscribeQms(notification);
    }

    @Override
    public void onItemClick(QmsTheme item) {
        presenter.onItemClick(item);
    }

    @Override
    public boolean onItemLongClick(QmsTheme item) {
        presenter.onItemLongClick(item);
        return false;
    }
}
