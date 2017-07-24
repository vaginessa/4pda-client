package forpdateam.ru.forpda.news

import forpdateam.ru.forpda.api.news.Constants
import forpdateam.ru.forpda.data.News
import forpdateam.ru.forpda.data.NewsItem

/**
 * Created by isanechek on 7/1/17.
 */

object NewsHelper {

    // for news details fragment
    const val NEWS_TAB = "news_tab"
    const val COMMENTS_TAB = "comments_tab"

    // for news list fragment
    const val OFFLINE_TAB = "offline_tab"
    const val LIST_TAB = "list_tab"
    const val CATEGORY_TAB = "category_tab"

    fun getTitleList() = mapOf(Constants.TAB_OFFLINE to "empty",
            Constants.TAB_ALL to Constants.NEWS_CATEGORY_ALL,
            Constants.TAB_ARTICLE to Constants.NEWS_CATEGORY_ARTICLES,
            Constants.TAB_REVIEWS to Constants.NEWS_CATEGORY_REVIEWS,
            Constants.TAB_SOFTWARE to Constants.NEWS_CATEGORY_SOFTWARE,
            Constants.TAB_GAMES to Constants.NEWS_CATEGORY_GAMES)

    fun mappingNewsItemToNews(item: NewsItem, category: String?) : News {
        val model = News()
        model.url = item.url
        model.imgUrl = item.imgUrl
        model.title = item.title
        model.author = item.author
        model.date = item.date
        model.commentsCount = item.commentsCount
        model.description = item.description
        category?.let { model.category = it }
        return model
    }

    fun mappingNewsItemsToNews(items: List<NewsItem>, category: String?) = items.map { mappingNewsItemToNews(it, category) }
}