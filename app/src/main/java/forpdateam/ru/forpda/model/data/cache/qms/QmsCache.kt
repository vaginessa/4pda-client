package forpdateam.ru.forpda.model.data.cache.qms

import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemesBd
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import io.realm.Realm

class QmsCache {

    fun saveContacts(items: List<QmsContact>) = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            realmTr.delete(QmsContactBd::class.java)
            realmTr.copyToRealmOrUpdate(items.map { QmsContactBd(it) })
        }
    }

    fun getContacts(): List<QmsContact> = Realm.getDefaultInstance().use {
        it.where(QmsContactBd::class.java).findAll().map { QmsContact(it) }
    }

    fun saveThemes(data: QmsThemes) = Realm.getDefaultInstance().use {
        it.executeTransaction({ realmTr ->
            realmTr.where(QmsThemesBd::class.java).equalTo("userId", data.userId).findAll().deleteAllFromRealm()
            realmTr.copyToRealmOrUpdate(QmsThemesBd(data))
        })
    }

    fun getThemes(userId: Int): QmsThemes = Realm.getDefaultInstance().use { realm ->
        realm.where(QmsThemesBd::class.java).equalTo("userId", userId).findAll().last().let { result ->
            QmsThemes(result).apply {
                themes.addAll(result.themes.map { QmsTheme(it) })
            }
        }
    }

}