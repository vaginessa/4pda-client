package forpdateam.ru.forpda.model.repository.qms

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.qms.QmsCache
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsApi
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.*

/**
 * Created by radiationx on 01.01.18.
 */

class QmsRepository(
        private val schedulers: SchedulersProvider,
        private val qmsApi: QmsApi,
        private val qmsCache: QmsCache,
        private val forumUsersCache: ForumUsersCache
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
            .doOnNext { saveUsers(it) }
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
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendNewTheme(nick: String, title: String, mess: String): Observable<QmsChatModel> = Observable
            .fromCallable { qmsApi.sendNewTheme(nick, title, mess) }
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


    private fun saveUsers(contacts: List<QmsContact>) {
        val forumUsers = contacts.map { contact ->
            ForumUser().apply {
                id = contact.id
                nick = contact.nick
                avatar = contact.avatar
            }
        }
        forumUsersCache.saveUsers(forumUsers)
    }


    /*
    *
    * cache
    *
    * */

    fun saveContactsCache(items: List<QmsContact>): Observable<List<QmsContact>> = Completable
            .fromCallable { qmsCache.saveContacts(items) }
            .toObservable<List<QmsContact>>()
            .flatMap { getContactsCache() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getContactsCache(): Observable<List<QmsContact>> = Observable
            .fromCallable { qmsCache.getContacts() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun saveThemesCache(data: QmsThemes): Observable<QmsThemes> = Completable
            .fromCallable { qmsCache.saveThemes(data) }
            .toObservable<QmsThemes>()
            .flatMap { getThemesCache(data.userId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getThemesCache(userId: Int): Observable<QmsThemes> = Observable
            .fromCallable { qmsCache.getThemes(userId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


}
