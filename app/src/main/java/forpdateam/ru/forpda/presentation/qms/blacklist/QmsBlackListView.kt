package forpdateam.ru.forpda.presentation.qms.blacklist

import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsContact

/**
 * Created by radiationx on 01.01.18.
 */

interface QmsBlackListView : IBaseView {
    fun showContacts(items: List<QmsContact>)
    fun showFoundUsers(items: List<ForumUser>)
    fun clearNickField()
    fun showItemDialogMenu(item: QmsContact)
}
