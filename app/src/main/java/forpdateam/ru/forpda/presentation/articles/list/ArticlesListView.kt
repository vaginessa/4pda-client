package forpdateam.ru.forpda.presentation.articles.list

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.news.NewsItem

/**
 * Created by radiationx on 01.01.18.
 */

interface ArticlesListView : IBaseView {
    fun showNews(items: List<NewsItem>, withClear: Boolean)
    fun showItemDialogMenu(item: NewsItem)
    fun showCreateNote(title: String, url: String)
}
