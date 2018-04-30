package forpdateam.ru.forpda.model.data.cache.history

import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd
import io.realm.Realm
import io.realm.Sort
import java.text.SimpleDateFormat
import java.util.*

class HistoryCache {

    private val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())

    fun add(id: Int, url: String, title: String) = Realm.getDefaultInstance().use {
        it.executeTransaction {realmTr ->
            val item = realmTr.where(HistoryItemBd::class.java).equalTo("id", id).findFirst()
            if (item == null) {
                realmTr.insert(HistoryItemBd().apply {
                    this.title = title
                    this.id = id
                    this.url = url
                    unixTime = System.currentTimeMillis()
                    date = dateFormat.format(Date(unixTime))
                })
            } else {
                item.url = url
                item.unixTime = System.currentTimeMillis()
                item.date = dateFormat.format(Date(item.getUnixTime()))
                realmTr.insertOrUpdate(item)
            }
        }
    }

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