package forpdateam.ru.forpda.model.repository.faviorites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import forpdateam.ru.forpda.entity.remote.events.NotificationEvent;
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi;
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting;
import forpdateam.ru.forpda.entity.remote.favorites.IFavItem;
import forpdateam.ru.forpda.entity.remote.favorites.FavData;
import forpdateam.ru.forpda.entity.remote.favorites.FavItem;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.entity.db.favorites.FavItemBd;
import forpdateam.ru.forpda.model.SchedulersProvider;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 01.01.18.
 */

public class FavoritesRepository {

    private SchedulersProvider schedulers;
    private FavoritesApi favoritesApi;

    public FavoritesRepository(SchedulersProvider schedulers, FavoritesApi favoritesApi) {
        this.schedulers = schedulers;
        this.favoritesApi = favoritesApi;
    }

    public Observable<FavData> loadFavorites(int st, boolean all, Sorting sorting) {
        return Observable
                .fromCallable(() -> favoritesApi.getFavorites(st, all, sorting))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<Boolean> editFavorites(int act, int favId, int id, String type) {
        switch (act) {
            case FavoritesApi.ACTION_EDIT_SUB_TYPE:
                return Observable.fromCallable(() -> favoritesApi.editSubscribeType(type, favId));
            case FavoritesApi.ACTION_EDIT_PIN_STATE:
                return Observable.fromCallable(() -> favoritesApi.editPinState(type, favId));
            case FavoritesApi.ACTION_DELETE:
                return Observable.fromCallable(() -> favoritesApi.delete(favId));
            case FavoritesApi.ACTION_ADD:
            case FavoritesApi.ACTION_ADD_FORUM:
                return Observable.fromCallable(() -> favoritesApi.add(id, act, type));
            default:
                return Observable.just(false);
        }
    }

    public Observable<List<FavItem>> getCache() {
        return Observable
                .fromCallable(() -> {
                    List<FavItem> items = new ArrayList<>();
                    try (Realm realm = Realm.getDefaultInstance()) {
                        RealmResults<FavItemBd> results = realm
                                .where(FavItemBd.class)
                                .findAll();
                        for (FavItemBd itemBd : results) {
                            items.add(new FavItem(itemBd));
                        }
                    }
                    return items;
                })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Completable saveFavorites(List<FavItem> items) {
        return Completable
                .fromRunnable(() -> {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.executeTransaction(r -> saveFavorites(r, items));
                    }
                })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Completable markRead(int topicId) {
        return Completable
                .fromRunnable(() -> {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.executeTransaction(realm1 -> {
                            IFavItem favItem = realm1
                                    .where(FavItemBd.class)
                                    .equalTo("topicId", topicId)
                                    .findFirst();
                            if (favItem != null) {
                                favItem.setNew(false);
                            }
                        });
                    }
                })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<Integer> handleEvent(TabNotification event, Sorting sorting, int count) {
        return Observable
                .fromCallable(() -> {
                    final int[] newCount = {0};
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.executeTransaction(realm1 -> {
                            newCount[0] = handleEventTransaction(realm, event, sorting, count);
                        });
                    }
                    return newCount[0];
                })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    private void saveFavorites(Realm realm, List<FavItem> items) {
        realm.delete(FavItemBd.class);
        List<FavItemBd> bdList = new ArrayList<>();
        for (FavItem item : items) {
            bdList.add(new FavItemBd(item));
        }
        realm.copyToRealmOrUpdate(bdList);
        bdList.clear();
    }

    private int handleEventTransaction(Realm realm, TabNotification event, Sorting sorting, int count) {
        RealmResults<FavItemBd> results = realm
                .where(FavItemBd.class)
                .findAll();
        ArrayList<FavItem> currentItems = new ArrayList<>();
        for (FavItemBd itemBd : results) {
            currentItems.add(new FavItem(itemBd));
        }

        NotificationEvent loadedEvent = event.getEvent();
        int id = loadedEvent.getSourceId();
        boolean isRead = loadedEvent.isRead();

        if (isRead) {
            count--;
            for (FavItem item : currentItems) {
                if (item.getTopicId() == id) {
                    item.setNew(false);
                    break;
                }
            }
        } else {
            count = event.getLoadedEvents().size();
            for (FavItem item : currentItems) {
                if (item.getTopicId() == id) {
                    if (item.getLastUserId() != ClientHelper.getUserId())
                        item.setNew(true);
                    item.setLastUserNick(loadedEvent.getUserNick());
                    item.setLastUserId(loadedEvent.getUserId());
                    item.setPin(loadedEvent.isImportant());
                    break;
                }
            }
            if (sorting.getKey().equals(Sorting.Key.TITLE)) {
                Collections.sort(currentItems, (o1, o2) -> {
                    if (sorting.getOrder().equals(Sorting.Order.ASC))
                        return o1.getTopicTitle().compareToIgnoreCase(o2.getTopicTitle());
                    return o2.getTopicTitle().compareToIgnoreCase(o1.getTopicTitle());
                });
            }

            if (sorting.getKey().equals(Sorting.Key.LAST_POST)) {
                for (FavItem item : currentItems) {
                    if (item.getTopicId() == id) {
                        currentItems.remove(item);
                        int index = 0;
                        if (sorting.getOrder().equals(Sorting.Order.ASC)) {
                            index = currentItems.size();
                        }
                        currentItems.add(index, item);
                        break;
                    }
                }
            }
        }
        saveFavorites(realm, currentItems);
        return count;
    }
}