package forpdateam.ru.forpda.presentation.qms.chat

import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage

/**
 * Created by radiationx on 01.01.18.
 */

interface QmsChatView : IBaseView {
    fun showChat(data: QmsChatModel)
    fun onNewThemeCreate(data: QmsChatModel)
    fun onSentMessage(items: List<QmsMessage>)
    fun onNewMessages(items: List<QmsMessage>)
    fun setMessageRefreshing(isRefreshing: Boolean)
    fun onBlockUser(res: Boolean)
    fun showCreateNote(name: String, nick: String, url: String)
    fun onUploadFiles(items: List<AttachmentItem>)
    fun showAvatar(avatarUrl: String)
}
