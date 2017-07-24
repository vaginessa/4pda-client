package forpdateam.ru.forpda.data

import forpdateam.ru.forpda.api.Api
import forpdateam.ru.forpda.api.Utils
import forpdateam.ru.forpda.api.regex.RegexStorage
import forpdateam.ru.forpda.ext.logger
import java.util.ArrayList
import java.util.regex.Pattern


import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ALL
import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ARTICLES
import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_GAMES
import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_GAMES
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_ANDROID
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_INTERVIEW
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_IOS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_WP
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_TABLETS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACCESSORIES_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACOUSTICS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ALL
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ARTICLES
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_GAMES
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_GAMES
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_ANDROID
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_INTERVIEW
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_IOS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_WP
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_NOTEBOOKS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMARTPHONES_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMART_WATCH_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_TABLETS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_SOFTWARE
/**
 * Created by isanechek on 7/20/17.
 */
object NewsApi4K {

    // public function

    fun getListNews(request: Request) : List<NewsItem> {
        return if (request.category != null) {
            getListNews(request.category, request.pageNumber)
        } else emptyList()
    }

    fun getListNews(category: String, pageNumber: Int = 0) : List<NewsItem> {
        val source = getSource(category, pageNumber)
        return getListNews(source)
    }

    fun getListNews(source: String?): List<NewsItem> {
        val cache = ArrayList<NewsItem>()
        val regex = RegexStorage.News.List.getListPattern()
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(source)
        while (matcher.find()) {
            val model = NewsItem()
            model.url = matcher.group(1)
            model.imgUrl = matcher.group(3)
            model.title = Utils.fromHtml(matcher.group(2))
            model.commentsCount = matcher.group(4)
            model.date = matcher.group(5)
            model.author = Utils.fromHtml(matcher.group(6))
            model.description = Utils.fromHtml(matcher.group(7))
            //            model.setTags(matcher.group(8));
            cache.add(model)
        }
        logger("mapping " + cache.size, null)
        return cache
    }

    fun getTopCommentsNews(source: String?) {

    }

    fun getSource(category: String, pageNumber: Int = 0) : String? {
        var url = getUrl(category)
        when { pageNumber >= 2 -> url += "page/$pageNumber/" }
        return getSource(url)
    }

    fun getSource(url: String) : String? = Api.getWebClient().get(url).body

    // private function
    private fun getUrl(category: String): String {
        when (category) {
            NEWS_CATEGORY_ALL -> return NEWS_URL_ALL
            NEWS_CATEGORY_ARTICLES -> return NEWS_URL_ARTICLES
            NEWS_CATEGORY_REVIEWS -> return NEWS_URL_REVIEWS
            NEWS_CATEGORY_SOFTWARE -> return NEWS_URL_SOFTWARE
            NEWS_CATEGORY_GAMES -> return NEWS_URL_GAMES
            NEWS_SUBCATEGORY_DEVSTORY_GAMES -> return NEWS_URL_DEVSTORY_GAMES
            NEWS_SUBCATEGORY_WP7_GAME -> return NEWS_URL_WP7_GAME
            NEWS_SUBCATEGORY_IOS_GAME -> return NEWS_URL_IOS_GAME
            NEWS_SUBCATEGORY_ANDROID_GAME -> return NEWS_URL_ANDROID_GAME
            NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE -> return NEWS_URL_DEVSTORY_SOFTWARE
            NEWS_SUBCATEGORY_WP7_SOFTWARE -> return NEWS_URL_WP7_SOFTWARE
            NEWS_SUBCATEGORY_IOS_SOFTWARE -> return NEWS_URL_IOS_SOFTWARE
            NEWS_SUBCATEGORY_ANDROID_SOFTWARE -> return NEWS_URL_ANDROID_SOFTWARE
            NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS -> return NEWS_URL_SMARTPHONES_REVIEWS
            NEWS_SUBCATEGORY_TABLETS_REVIEWS -> return NEWS_URL_TABLETS_REVIEWS
            NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS -> return NEWS_URL_SMART_WATCH_REVIEWS
            NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS -> return NEWS_URL_ACCESSORIES_REVIEWS
            NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS -> return NEWS_URL_NOTEBOOKS_REVIEWS
            NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS -> return NEWS_URL_ACOUSTICS_REVIEWS
            NEWS_SUBCATEGORY_HOW_TO_ANDROID -> return NEWS_URL_HOW_TO_ANDROID
            NEWS_SUBCATEGORY_HOW_TO_IOS -> return NEWS_URL_HOW_TO_IOS
            NEWS_SUBCATEGORY_HOW_TO_WP -> return NEWS_URL_HOW_TO_WP
            NEWS_SUBCATEGORY_HOW_TO_INTERVIEW -> return NEWS_URL_HOW_TO_INTERVIEW
        }
        return NEWS_URL_ALL
    }

//
//    companion object {
//        private const val TAG = "NewsApi4K"
//        var INSTANCE: NewsApi4K? = null
//        fun createInstance() { INSTANCE = NewsApi4K() }
//        fun  getInstance(): NewsApi4K = INSTANCE ?: throw IllegalStateException("No Created News Api Instance!!!")
//    }
}