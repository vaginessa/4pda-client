package forpdateam.ru.forpda.model.repository.history

import java.util.ArrayList

import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

/**
 * Created by radiationx on 01.01.18.
 */

class HistoryRepository(
        private val schedulers: SchedulersProvider
) {

    fun getHistory(): Observable<List<HistoryItem>> = Observable
            .fromCallable<List<HistoryItem>> {
                val items = ArrayList<HistoryItem>()
                Realm.getDefaultInstance().use { realm ->
                    val results = realm
                            .where(HistoryItemBd::class.java)
                            .findAllSorted("unixTime", Sort.DESCENDING)
                    for (itemBd in results) {
                        items.add(HistoryItem(itemBd))
                    }
                }
                items
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun remove(id: Int): Completable = Completable
            .fromRunnable {
                Realm.getDefaultInstance().use { realm ->
                    realm.executeTransaction { realm1 ->
                        realm1.where(HistoryItemBd::class.java)
                                .equalTo("id", id)
                                .findAll()
                                .deleteAllFromRealm()
                    }
                }
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    fun clear(): Completable = Completable
            .fromRunnable {
                Realm.getDefaultInstance().use { realm ->
                    realm.executeTransaction { realm1 ->
                        realm1.delete(HistoryItemBd::class.java)
                    }
                }
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
