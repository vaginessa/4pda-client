package forpdateam.ru.forpda.model.data.cache.history

import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd
import io.realm.Realm
import io.realm.Sort

class HistoryCache {

    fun getHistory(): List<HistoryItem> = Realm.getDefaultInstance().use {
        it.where(HistoryItemBd::class.java).findAllSorted("unixTime", Sort.DESCENDING).map { HistoryItem(it) }
    }

    fun remove(id: Int) = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            realmTr.where(HistoryItemBd::class.java).equalTo("id", id).findAll().deleteAllFromRealm()
        }
    }

    fun clear() = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            realmTr.delete(HistoryItemBd::class.java)
        }
    }
}