package forpdateam.ru.forpda.presentation.articles.detail.content

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage

/**
 * Created by radiationx on 01.01.18.
 */

interface ArticleContentView : IBaseView {
    fun showData(article: DetailsPage)
}
