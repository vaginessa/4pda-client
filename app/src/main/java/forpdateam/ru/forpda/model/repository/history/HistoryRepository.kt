package forpdateam.ru.forpda.model.repository.history

import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Created by radiationx on 01.01.18.
 */

class HistoryRepository(
        private val schedulers: SchedulersProvider,
        private val historyCache: HistoryCache
) {

    fun getHistory(): Observable<List<HistoryItem>> = Observable
            .fromCallable { historyCache.getHistory() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun remove(id: Int): Completable = Completable
            .fromRunnable { historyCache.remove(id) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun clear(): Completable = Completable
            .fromRunnable { historyCache.clear() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
