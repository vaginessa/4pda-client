package forpdateam.ru.forpda.model.repository.qms

import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemesBd
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.*
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsApi
import forpdateam.ru.forpda.model.repository.temp.TempHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Realm
import java.util.*

/**
 * Created by radiationx on 01.01.18.
 */

class QmsRepository(
        private val schedulers: SchedulersProvider,
        private val qmsApi: QmsApi
) {

    //Common
    fun findUser(nick: String): Observable<List<ForumUser>> = Observable
            .fromCallable { qmsApi.findUser(nick) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun blockUser(nick: String): Observable<ArrayList<QmsContact>> = Observable
            .fromCallable { qmsApi.blockUser(nick) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun unBlockUsers(userId: Int): Observable<ArrayList<QmsContact>> = Observable
            .fromCallable { qmsApi.unBlockUsers(userId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    //Contacts
    fun getContactList(): Observable<List<QmsContact>> = Observable
            .fromCallable { qmsApi.contactList }
            .map { TempHelper.interceptContacts(it) }
            .flatMap { saveContactsCache(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getBlackList(): Observable<ArrayList<QmsContact>> = Observable
            .fromCallable { qmsApi.blackList }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deleteDialog(mid: Int): Observable<String> = Observable
            .fromCallable { qmsApi.deleteDialog(mid) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    //Themes
    fun getThemesList(id: Int): Observable<QmsThemes> = Observable
            .fromCallable { qmsApi.getThemesList(id) }
            .flatMap { saveThemesCache(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deleteTheme(id: Int, themeId: Int): Observable<QmsThemes> = Observable
            .fromCallable { qmsApi.deleteTheme(id, themeId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    //Chat
    fun getChat(userId: Int, themeId: Int): Observable<QmsChatModel> = Observable
            .fromCallable { qmsApi.getChat(userId, themeId) }
            .map { TempHelper.transform(it, false) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendNewTheme(nick: String, title: String, mess: String): Observable<QmsChatModel> = Observable
            .fromCallable { qmsApi.sendNewTheme(nick, title, mess) }
            .map { TempHelper.transform(it, false) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendMessage(userId: Int, themeId: Int, text: String): Observable<ArrayList<QmsMessage>> = Observable
            .fromCallable { qmsApi.sendMessage(userId, themeId, text) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getMessagesFromWs(themeId: Int, messageId: Int, afterMessageId: Int): Observable<ArrayList<QmsMessage>> = Observable
            .fromCallable { qmsApi.getMessagesFromWs(themeId, messageId, afterMessageId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getMessagesAfter(userId: Int, themeId: Int, afterMessageId: Int): Observable<ArrayList<QmsMessage>> = Observable
            .fromCallable { qmsApi.getMessagesAfter(userId, themeId, afterMessageId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>): Observable<List<AttachmentItem>> = Observable
            .fromCallable { qmsApi.uploadFiles(files, pending) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())





    /*
    *
    * cache
    *
    * */

    fun saveContactsCache(items: List<QmsContact>): Observable<List<QmsContact>> = Completable
            .fromCallable {
                Realm.getDefaultInstance().use { realm ->
                    realm.executeTransaction { r ->
                        r.delete(QmsContactBd::class.java)
                        val bdList = ArrayList<QmsContactBd>()
                        for (contact in items) {
                            bdList.add(QmsContactBd(contact))
                        }
                        r.copyToRealmOrUpdate(bdList)
                        bdList.clear()
                    }
                }
            }
            .toObservable<List<QmsContact>>()
            .flatMap { getContactsCache() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getContactsCache(): Observable<List<QmsContact>> = Observable
            .fromCallable {
                val currentItems = mutableListOf<QmsContact>()
                Realm.getDefaultInstance().use { realm ->
                    val results = realm.where(QmsContactBd::class.java).findAll()
                    for (qmsContactBd in results) {
                        val contact = QmsContact(qmsContactBd)
                        currentItems.add(contact)
                    }
                }
                currentItems as List<QmsContact>
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun saveThemesCache(data: QmsThemes): Observable<QmsThemes> = Completable
            .fromCallable {
                Realm.getDefaultInstance().use { realm ->
                    realm.executeTransaction({ r ->
                        r.where(QmsThemesBd::class.java)
                                .equalTo("userId", data.getUserId())
                                .findAll()
                                .deleteAllFromRealm()
                        val qmsThemesBd = QmsThemesBd(data)
                        r.copyToRealmOrUpdate(qmsThemesBd)
                        qmsThemesBd.themes.clear()
                    })
                }
            }
            .toObservable<QmsThemes>()
            .flatMap { getThemesCache(data.userId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getThemesCache(userId: Int): Observable<QmsThemes> = Observable
            .fromCallable {
                Realm.getDefaultInstance().use { realm ->
                    val results = realm
                            .where(QmsThemesBd::class.java)
                            .equalTo("userId", userId)
                            .findAll()
                            .last()
                    QmsThemes(results).apply {
                        themes.addAll(results.themes.map { QmsTheme(it) })
                    }
                }
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


}
