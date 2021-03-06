package forpdateam.ru.forpda.ui.fragments.qms;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.data.models.TabNotification;
import forpdateam.ru.forpda.data.realm.qms.QmsContactBd;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsContactsAdapter;
import forpdateam.ru.forpda.ui.fragments.qms.chat.QmsChatFragment;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsContactsFragment extends RecyclerFragment implements QmsContactsAdapter.OnItemClickListener<IQmsContact> {
    private QmsContactsAdapter adapter;
    private Realm realm;
    private RealmResults<QmsContactBd> results;
    private DynamicDialogMenu<QmsContactsFragment, IQmsContact> dialogMenu;

    private Observer notification = (observable, o) -> {
        if (o == null) return;
        TabNotification event = (TabNotification) o;
        runInUiThread(() -> handleEvent(event));
    };

    public QmsContactsFragment() {
        configuration.setAlone(true);
        configuration.setMenu(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_contacts));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
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
        viewsReady();
        initFabBehavior();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        recyclerView.addOnScrollListener(pauseOnScrollListener);


        fab.setImageDrawable(App.getVecDrawable(getContext(), R.drawable.ic_fab_create));
        fab.setOnClickListener(view1 -> TabManager.get().add(QmsChatFragment.class));
        fab.setVisibility(View.VISIBLE);

        adapter = new QmsContactsAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        bindView();
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
            private ArrayList<QmsContactBd> searchContacts = new ArrayList<>();

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchContacts.clear();
                if (!newText.isEmpty()) {
                    for (QmsContactBd contact : results) {
                        if (contact.getNick().toLowerCase().contains(newText.toLowerCase()))
                            searchContacts.add(contact);
                    }
                    adapter.addAll(searchContacts);
                } else {
                    adapter.addAll(results);
                }
                return false;
            }
        });
        searchView.setQueryHint(getString(R.string.user));
        menu.add(R.string.blacklist)
                .setOnMenuItemClickListener(item -> {
                    TabManager.get().add(QmsBlackListFragment.class);
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
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        setRefreshing(true);
        subscribe(RxApi.Qms().getContactList(), this::onLoadContacts, new ArrayList<>(), v -> loadData());
        return true;
    }

    private void onLoadContacts(ArrayList<QmsContact> data) {
        setRefreshing(false);
        recyclerView.scrollToPosition(0);

        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.delete(QmsContactBd.class);
            List<QmsContactBd> bdList = new ArrayList<>();
            for (QmsContact contact : data) {
                bdList.add(new QmsContactBd(contact));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::bindView);
    }

    private void bindView() {
        if (realm.isClosed()) return;
        results = realm.where(QmsContactBd.class).findAll();

        if (results.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_contacts)
                        .setTitle(R.string.funny_contacts_nodata_title);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }

        ArrayList<QmsContact> currentItems = new ArrayList<>();
        for (QmsContactBd qmsContactBd : results) {
            QmsContact contact = new QmsContact(qmsContactBd);
            currentItems.add(contact);
        }
        int count = 0;
        for (QmsContact contact : currentItems) {
            if (contact.getCount() > 0) {
                count += contact.getCount();
            }
        }

        ClientHelper.setQmsCount(count);
        ClientHelper.get().notifyCountsChanged();

        adapter.addAll(currentItems);
    }

    private void handleEvent(TabNotification event) {
        bindView();
        if (true) return;
        SparseIntArray sparseArray = new SparseIntArray();

        if (realm.isClosed()) return;
        results = realm.where(QmsContactBd.class).findAll();

        ArrayList<QmsContact> currentItems = new ArrayList<>();
        for (QmsContactBd qmsContactBd : results) {
            QmsContact contact = new QmsContact(qmsContactBd);
            currentItems.add(contact);
        }

        for (NotificationEvent loadedEvent : event.getLoadedEvents()) {
            int count = sparseArray.get(loadedEvent.getUserId());
            count += loadedEvent.getMsgCount();
            sparseArray.put(loadedEvent.getUserId(), count);
        }
        for (int i = sparseArray.size() - 1; i >= 0; i--) {
            int id = sparseArray.keyAt(i);
            int count = sparseArray.valueAt(i);
            for (QmsContact item : currentItems) {
                if (item.getId() == id) {
                    item.setCount(count);
                    Collections.swap(currentItems, currentItems.indexOf(item), 0);
                    break;
                }
            }
        }

        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.delete(QmsContactBd.class);
            List<QmsContactBd> bdList = new ArrayList<>();
            for (QmsContact qmsContact : currentItems) {
                bdList.add(new QmsContactBd(qmsContact));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::bindView);

        //adapter.notifyDataSetChanged();
        /*ArrayList<IFavItem> newItems = new ArrayList<>();
        newItems.addAll(currentItems);
        refreshList(newItems);*/
    }

    public void updateCount(int id, int count) {
        /*for (QmsContact item : currentItems) {
            if (item.getId() == id) {
                item.setCount(count);
                break;
            }
        }
        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.delete(QmsContactBd.class);
            List<QmsContactBd> bdList = new ArrayList<>();
            for (QmsContact qmsContact : currentItems) {
                bdList.add(new QmsContactBd(qmsContact));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::bindView);*/
    }

    public void deleteDialog(int mid) {
        setRefreshing(true);
        subscribe(RxApi.Qms().deleteDialog(mid), this::onDeletedDialog, "");
    }

    private void onDeletedDialog(String res) {
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        QmsHelper.get().unSubscribeQms(notification);
    }

    @Override
    public void onItemClick(IQmsContact item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getNick());
        args.putInt(QmsThemesFragment.USER_ID_ARG, item.getId());
        args.putString(QmsThemesFragment.USER_AVATAR_ARG, item.getAvatar());
        TabManager.get().add(QmsThemesFragment.class, args);
    }

    @Override
    public boolean onItemLongClick(IQmsContact item) {
        if (dialogMenu == null) {
            dialogMenu = new DynamicDialogMenu<>();
            dialogMenu.addItem(getString(R.string.profile), (context, data) -> {
                IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + data.getId());
            });
            dialogMenu.addItem(getString(R.string.add_to_blacklist), (context, data) -> {
                subscribe(RxApi.Qms().blockUser(data.getNick()), qmsContacts -> {
                    if (!qmsContacts.isEmpty()) {
                        Toast.makeText(getContext(), R.string.user_added_to_blacklist, Toast.LENGTH_SHORT).show();
                    }
                }, new ArrayList<>());
            });
            dialogMenu.addItem(getString(R.string.delete), (context, data) -> context.deleteDialog(data.getId()));
            dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> {
                String title = String.format(getString(R.string.dialogs_Nick), data.getNick());
                String url = "https://4pda.ru/forum/index.php?act=qms&mid=" + data.getId();
                NotesAddPopup.showAddNoteDialog(context1.getContext(), title, url);
            });
        }
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), QmsContactsFragment.this, item);
        return false;
    }
}
