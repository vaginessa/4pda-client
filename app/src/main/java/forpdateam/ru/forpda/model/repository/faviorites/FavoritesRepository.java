package forpdateam.ru.forpda.model.repository.faviorites;

import java.util.Collections;
import java.util.List;

import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent;
import forpdateam.ru.forpda.entity.remote.favorites.FavData;
import forpdateam.ru.forpda.entity.remote.favorites.FavItem;
import forpdateam.ru.forpda.model.SchedulersProvider;
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache;
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi;
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by radiationx on 01.01.18.
 */

public class FavoritesRepository {

    private SchedulersProvider schedulers;
    private FavoritesApi favoritesApi;
    private FavoritesCache favoritesCache;

    public FavoritesRepository(SchedulersProvider schedulers, FavoritesApi favoritesApi, FavoritesCache favoritesCache) {
        this.schedulers = schedulers;
        this.favoritesApi = favoritesApi;
        this.favoritesCache = favoritesCache;
    }

    public Observable<FavData> loadFavorites(int st, boolean all, Sorting sorting) {
        return Observable
                .fromCallable(() -> favoritesApi.getFavorites(st, all, sorting))
                .doOnNext(favData -> {
                    favoritesCache.saveFavorites(favData.getItems());
                })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<List<FavItem>> getCache() {
        return Observable
                .fromCallable(() -> favoritesCache.getItems())
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

    public Completable markRead(int topicId) {
        return Completable
                .fromRunnable(() -> {
                    FavItem favItem = favoritesCache.getItemByTopicId(topicId);
                    if (favItem != null) {
                        favItem.setNew(false);
                        favoritesCache.updateItem(favItem);
                    }
                })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<Integer> handleEvent(TabNotification event, Sorting sorting, int count) {
        return Observable
                .fromCallable(() -> {
                    List<FavItem> favItems = favoritesCache.getItems();
                    return handleEventTransaction(favItems, event, sorting, count);
                })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }


    private int handleEventTransaction(List<FavItem> favItems, TabNotification event, Sorting sorting, int count) {
        NotificationEvent loadedEvent = event.getEvent();
        int id = loadedEvent.getSourceId();
        boolean isRead = loadedEvent.isRead();

        if (isRead) {
            count--;
            for (FavItem item : favItems) {
                if (item.getTopicId() == id) {
                    item.setNew(false);
                    break;
                }
            }
        } else {
            count = event.getLoadedEvents().size();
            for (FavItem item : favItems) {
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
                Collections.sort(favItems, (o1, o2) -> {
                    if (sorting.getOrder().equals(Sorting.Order.ASC))
                        return o1.getTopicTitle().compareToIgnoreCase(o2.getTopicTitle());
                    return o2.getTopicTitle().compareToIgnoreCase(o1.getTopicTitle());
                });
            }

            if (sorting.getKey().equals(Sorting.Key.LAST_POST)) {
                for (FavItem item : favItems) {
                    if (item.getTopicId() == id) {
                        favItems.remove(item);
                        int index = 0;
                        if (sorting.getOrder().equals(Sorting.Order.ASC)) {
                            index = favItems.size();
                        }
                        favItems.add(index, item);
                        break;
                    }
                }
            }
        }
        favoritesCache.saveFavorites(favItems);
        return count;
    }
}
