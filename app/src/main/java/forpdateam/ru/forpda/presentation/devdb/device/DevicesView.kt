package forpdateam.ru.forpda.presentation.devdb.device

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData

/**
 * Created by radiationx on 01.01.18.
 */

interface DevicesView : IBaseView {
    fun showData(data: Brand)
    fun showCreateNote(title: String, url: String)
}
