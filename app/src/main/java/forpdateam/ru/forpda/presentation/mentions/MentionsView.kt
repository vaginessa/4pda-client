package forpdateam.ru.forpda.presentation.mentions

import forpdateam.ru.forpda.api.mentions.models.MentionItem
import forpdateam.ru.forpda.api.mentions.models.MentionsData
import forpdateam.ru.forpda.common.mvp.IBaseView

/**
 * Created by radiationx on 01.01.18.
 */

interface MentionsView : IBaseView {
    fun showMentions(data: MentionsData)
    fun showItemDialogMenu(item: MentionItem)
    fun showAddFavoritesDialog(id: Int)
}
