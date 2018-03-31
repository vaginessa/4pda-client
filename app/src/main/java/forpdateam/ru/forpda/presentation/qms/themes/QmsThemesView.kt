package forpdateam.ru.forpda.presentation.qms.themes

import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes

/**
 * Created by radiationx on 01.01.18.
 */

interface QmsThemesView : IBaseView {
    fun showThemes(data: QmsThemes)
    fun showAvatar(avatarUrl: String)
    fun showCreateNote(nick: String, url: String)
    fun showCreateNote(name: String, nick: String, url: String)
    fun onBlockUser(res: Boolean)
    fun showItemDialogMenu(item: QmsTheme)
}
