package forpdateam.ru.forpda.presentation.devdb.device

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData

/**
 * Created by radiationx on 01.01.18.
 */

interface DeviceView : IBaseView {
    fun showData(data: Device)
    fun showCreateNote(title: String, url: String)
}
