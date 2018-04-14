package forpdateam.ru.forpda.presentation.devdb.brands

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData

/**
 * Created by radiationx on 01.01.18.
 */

interface BrandsView : IBaseView {
    fun showData(data: Brands)
    fun initCategories(categories: Array<String>, position: Int)
}
