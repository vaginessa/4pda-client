package forpdateam.ru.forpda.model.data.cache.notes

import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.entity.db.notes.NoteItemBd
import io.realm.Realm
import io.realm.Sort

class NotesCache {

    fun getItems() = Realm.getDefaultInstance().use {
        it.where(NoteItemBd::class.java).findAllSorted("id", Sort.DESCENDING).map { NoteItem(it) }
    }

    fun update(item: NoteItem) = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            val itemBd = realmTr.where(NoteItemBd::class.java).equalTo("id", item.id).findFirst()?.apply {
                title = item.getTitle()
                link = item.getLink()
                content = item.getContent()
            } ?: NoteItemBd(item)
            realmTr.insertOrUpdate(itemBd)
        }
    }

    fun delete(id: Long) = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            realmTr.where(NoteItemBd::class.java).equalTo("id", id).findAll().deleteAllFromRealm()
        }
    }

    fun add(item: NoteItem) = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            realmTr.insertOrUpdate(NoteItemBd(item))
        }
    }

    fun add(items: List<NoteItem>) = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            realmTr.insertOrUpdate(items.map { NoteItemBd(it) })
        }
    }

}