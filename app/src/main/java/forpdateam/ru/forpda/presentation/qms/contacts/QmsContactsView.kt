package forpdateam.ru.forpda.presentation.qms.contacts

import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.qms.QmsContact

/**
 * Created by radiationx on 01.01.18.
 */

interface QmsContactsView : IBaseView {
    fun showContacts(items: List<QmsContact>)
    fun onBlockUser(res: Boolean)
    fun showCreateNote(nick: String, url: String)
}
