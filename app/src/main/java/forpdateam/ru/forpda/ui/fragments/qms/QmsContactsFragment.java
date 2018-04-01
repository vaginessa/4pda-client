package forpdateam.ru.forpda.ui.fragments.qms;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
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

import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.entity.remote.qms.QmsContact;
import forpdateam.ru.forpda.presentation.qms.contacts.QmsContactsPresenter;
import forpdateam.ru.forpda.presentation.qms.contacts.QmsContactsView;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsContactsAdapter;
import forpdateam.ru.forpda.ui.fragments.qms.chat.QmsChatFragment;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsContactsFragment extends RecyclerFragment implements QmsContactsAdapter.OnItemClickListener<QmsContact>, QmsContactsView {
    private QmsContactsAdapter adapter;
    private DynamicDialogMenu<QmsContactsFragment, QmsContact> dialogMenu = new DynamicDialogMenu<>();

    private Observer notification = (observable, o) -> {
        if (o == null) return;
        TabNotification event = (TabNotification) o;
        //runInUiThread(() -> handleEvent(event));
    };

    @InjectPresenter
    QmsContactsPresenter presenter;

    @ProvidePresenter
    QmsContactsPresenter providePresenter() {
        return new QmsContactsPresenter(App.get().Di().getQmsRepository());
    }

    public QmsContactsFragment() {
        configuration.setAlone(true);
        configuration.setMenu(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_contacts));
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
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        recyclerView.addOnScrollListener(pauseOnScrollListener);


        fab.setImageDrawable(App.getVecDrawable(getContext(), R.drawable.ic_fab_create));
        fab.setOnClickListener(view1 -> TabManager.get().add(QmsChatFragment.class));
        fab.setVisibility(View.VISIBLE);


        dialogMenu.addItem(getString(R.string.profile), (context, data) -> presenter.openProfile(data));
        dialogMenu.addItem(getString(R.string.add_to_blacklist), (context, data) -> presenter.blockUser(data.getNick()));
        dialogMenu.addItem(getString(R.string.delete), (context, data) -> presenter.deleteDialog(data.getId()));
        dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> presenter.createNote(data));

        adapter = new QmsContactsAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        QmsHelper.get().subscribeQms(notification);
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        toolbar.inflateMenu(R.menu.qms_contacts_menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

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
                presenter.searchLocal(newText);
                return false;
            }
        });
        searchView.setQueryHint(getString(R.string.user));
        menu.add(R.string.blacklist)
                .setOnMenuItemClickListener(item -> {
                    presenter.openBlackList();
                    return false;
                });
    }

    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        if (getMenu().findItem(R.id.action_search).isActionViewExpanded()) {
            recyclerView.setAdapter(adapter);
            toolbar.collapseActionView();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void showContacts(@NotNull List<? extends QmsContact> items) {
        recyclerView.scrollToPosition(0);

        int count = 0;
        for (QmsContact contact : items) {
            if (contact.getCount() > 0) {
                count += contact.getCount();
            }
        }

        ClientHelper.setQmsCount(count);
        ClientHelper.get().notifyCountsChanged();

        adapter.addAll(items);
    }

    @Override
    public void onBlockUser(boolean res) {
        if (res) {
            Toast.makeText(getContext(), R.string.user_added_to_blacklist, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showCreateNote(@NotNull String nick, @NotNull String url) {
        String title = String.format(getString(R.string.dialogs_Nick), nick);
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
    }

    @Override
    public void showItemDialogMenu(@NotNull QmsContact item) {
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), QmsContactsFragment.this, item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QmsHelper.get().unSubscribeQms(notification);
    }

    @Override
    public void onItemClick(QmsContact item) {
        presenter.onItemClick(item);
    }

    @Override
    public boolean onItemLongClick(QmsContact item) {
        presenter.onItemLongClick(item);
        return false;
    }
}
