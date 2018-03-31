package forpdateam.ru.forpda.model.repository.qms

import biz.source_code.miniTemplator.MiniTemplator
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsApi
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.apirx.ForumUsersCache
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.FunnyContent
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Realm
import java.util.ArrayList

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
            .map { interceptContacts(it) }
            .flatMap { saveCache(it) }
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
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deleteTheme(id: Int, themeId: Int): Observable<QmsThemes> = Observable
            .fromCallable { qmsApi.deleteTheme(id, themeId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    //Chat
    fun getChat(userId: Int, themeId: Int): Observable<QmsChatModel> = Observable
            .fromCallable { qmsApi.getChat(userId, themeId) }
            .map { transform(it, false) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendNewTheme(nick: String, title: String, mess: String): Observable<QmsChatModel> = Observable
            .fromCallable { qmsApi.sendNewTheme(nick, title, mess) }
            .map { transform(it, false) }
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


    private fun interceptContacts(contacts: ArrayList<QmsContact>): ArrayList<QmsContact> {
        val forumUsers = ArrayList<ForumUser>()
        for (post in contacts) {
            val forumUser = ForumUser()
            forumUser.id = post.id
            forumUser.nick = post.nick
            forumUser.avatar = post.avatar
        }
        ForumUsersCache.saveUsers(forumUsers)
        return contacts
    }

    fun transform(chatModel: QmsChatModel, withHtml: Boolean): QmsChatModel {
        if (withHtml) {
            val t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT)
            App.setTemplateResStrings(t)
            t!!.setVariableOpt("style_type", App.get().cssStyleType)
            t.setVariableOpt("chat_title", ApiUtils.htmlEncode(chatModel.title))
            t.setVariableOpt("chatId", chatModel.themeId)
            t.setVariableOpt("userId", chatModel.userId)
            t.setVariableOpt("nick", chatModel.nick)
            t.setVariableOpt("avatarUrl", chatModel.avatarUrl)

            val endIndex = chatModel.messages.size
            val startIndex = Math.max(endIndex - 30, 0)
            chatModel.showedMessIndex = startIndex
            val messTemp = App.get().getTemplate(App.TEMPLATE_QMS_CHAT_MESS)
            App.setTemplateResStrings(t)
            generateMess(messTemp, chatModel.messages, startIndex, endIndex)
            t.setVariableOpt("messages", messTemp!!.generateOutput())
            messTemp.reset()
            chatModel.html = t.generateOutput()
            t.reset()
        }
        return chatModel
    }

    private fun generateMess(t: MiniTemplator?, messages: List<QmsMessage>, start: Int, end: Int): MiniTemplator? {
        for (i in start until end) {
            val mess = messages[i]
            generateMess(t, mess)
        }
        return t
    }

    private fun generateMess(t: MiniTemplator?, mess: QmsMessage): MiniTemplator {
        if (mess.isDate) {
            t!!.setVariableOpt("date", mess.date)
            t.addBlockOpt("date")
        } else {
            t!!.setVariableOpt("from_class", if (mess.isMyMessage) "our" else "his")
            t.setVariableOpt("unread_class", if (mess.readStatus) "" else "unread")
            t.setVariableOpt("mess_id", mess.id)
            t.setVariableOpt("content", mess.content)
            t.setVariableOpt("time", mess.time)
            t.addBlockOpt("mess")
        }
        t.addBlockOpt("item")

        return t
    }


    /*
    *
    * cache
    *
    * */

    fun saveCache(items: List<QmsContact>): Observable<List<QmsContact>> = Completable
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
            .flatMap { getCache() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    fun getCache(): Observable<List<QmsContact>> = Observable
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


}
