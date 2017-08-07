package forpdateam.ru.forpda.data


import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.api.Api
import forpdateam.ru.forpda.api.Utils
import forpdateam.ru.forpda.api.news.Constants.*
import forpdateam.ru.forpda.api.regex.RegexStorage
import forpdateam.ru.forpda.ext.logger
import forpdateam.ru.forpda.utils.NewsHtmlBuilder
import forpdateam.ru.forpda.utils.html
import io.reactivex.Single
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*
import java.util.regex.Pattern

/**
 * Created by isanechek on 7/20/17.
 */
object NewsApi4K {

    private val TAG = NewsApi4K::class.java.simpleName
    const val EMPTY_OR_NULL_RESPONSE_FROM_NETWORK = "newtrork.null"

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

    fun getNews(source: String?) : News {
        val regex = RegexStorage.News.Details.getRootDetailsPattern()
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(source)
        val model = News()
        while (matcher.find()) {
            model.body = NewsHtmlBuilder.transformBody(matcher.group(1))
            model.moreNews = matcher.group(2)
            model.navId = matcher.group(3)
            model.comments = matcher.group(5)
        }
        return model
    }

    private fun transformationPage(source: String?) : String {
        logger("$TAG transformationPage source $source")
        var response = ""
        source?.let {
           val re = html {
              body { +source }
           }

            response = re.toString()
        }
        return response
    }

    /*Этот стыд нужно переписать. Ибо тормозит весь процесс движения к счастью.*/
    private fun getMoreStuff(source: String) : List<OtherNews> {
        val cache = mutableListOf<OtherNews>()
        val doc: Document = Jsoup.parse(source)
        val elements = doc.body().select("aside")
        for(i in 0 until elements.size) {
            val element = elements[i] as Element
            for (j in 0 until element.children().size) {
                val el = element.children()[j] as Element
                for (l in 0 until el.children().size) {
                    val tag = el.children()[l] as Element
                    var category = ""
                    when {
                        tag.tagName() == "h2" -> category = tag.text()
                        tag.tagName() == "ul" -> if (category == "Самые комментируемые") {
                            for (q in 0 until tag.children().size) {
                                val model = OtherNews()
                                val e = tag.children()[q] as Element
                                model.title = e.getElementsByClass("title").select("a").attr("title")
                                model.url = e.getElementsByClass("title").select("a").attr("href")
                                model.commentsCount = e.getElementsByClass("price-slider").select("a").text()
                                cache.add(model)
                            }
                        }
                    }
                }
            }
        }
        return cache
    }

    fun getSingleSource(request: Request): Single<String>  = Single.fromCallable { request.url?.let { getSource(it) } }

    fun getSource(category: String, pageNumber: Int = 0) : String? {
        var url = getUrl(category)
        when { pageNumber >= 2 -> url += "page/$pageNumber/" }
        return getSource(url)
    }

    // чтобы блядь не было null
    fun getSource(url: String) : String = Api.getWebClient().get(url.replace("\"//".toRegex(), "\"http://")).body ?: EMPTY_OR_NULL_RESPONSE_FROM_NETWORK

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

    val categoryList: List<Category>
        get() = listOf(
                Category(R.string.category_news, generalSubList),
                Category(R.string.category_article, articlesSubList),
                Category(R.string.category_reviews, reviewsSubList),
                Category(R.string.category_software, softwareSubList),
                Category(R.string.category_games, gamesSubList))

    private val generalSubList = listOf(SubCategory(R.string.sub_category_all, NEWS_CATEGORY_ALL))

    private val gamesSubList = listOf(
            SubCategory(R.string.sub_category_all, NEWS_CATEGORY_ARTICLES),
            SubCategory(R.string.games_android, NEWS_SUBCATEGORY_ANDROID_GAME),
            SubCategory(R.string.games_ios, NEWS_SUBCATEGORY_IOS_GAME),
            SubCategory(R.string.games_wp, NEWS_SUBCATEGORY_WP7_GAME),
            SubCategory(R.string.games_story, NEWS_SUBCATEGORY_DEVSTORY_GAMES))

    private val softwareSubList = listOf(
            SubCategory(R.string.sub_category_all, NEWS_CATEGORY_SOFTWARE),
            SubCategory(R.string.software_android, NEWS_SUBCATEGORY_ANDROID_SOFTWARE),
            SubCategory(R.string.software_ios, NEWS_SUBCATEGORY_IOS_SOFTWARE),
            SubCategory(R.string.software_wp, NEWS_SUBCATEGORY_WP7_SOFTWARE),
            SubCategory(R.string.software_story, NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE))

    private val reviewsSubList = listOf(
            SubCategory(R.string.sub_category_all, NEWS_CATEGORY_REVIEWS),
            SubCategory(R.string.reviews_phones, NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS),
            SubCategory(R.string.reviews_tablets, NEWS_SUBCATEGORY_TABLETS_REVIEWS),
            SubCategory(R.string.reviews_watch, NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS),
            SubCategory(R.string.reviews_accessories, NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS),
            SubCategory(R.string.reviews_notebooks, NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS),
            SubCategory(R.string.reviews_acoustics, NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS))

    private val articlesSubList = listOf(
            SubCategory(R.string.sub_category_all, NEWS_CATEGORY_ARTICLES),
            SubCategory(R.string.article_android, NEWS_SUBCATEGORY_HOW_TO_ANDROID),
            SubCategory(R.string.article_ios, NEWS_SUBCATEGORY_HOW_TO_IOS),
            SubCategory(R.string.article_wp, NEWS_SUBCATEGORY_HOW_TO_WP),
            SubCategory(R.string.article_interview, NEWS_SUBCATEGORY_HOW_TO_INTERVIEW))

    // DETAILS

    // Делает что хорое и полезное
    fun mappingDetailsMoreNews(html: String) : ArrayList<OtherNews> {
        val cache = ArrayList<OtherNews>()
        val doc: Document = Jsoup.parse(html)
        val elements = doc.select("li")
        (0 until elements.size).forEach { i ->
            val element = elements[i] as Element
            val model = OtherNews()
            model.url = element.select("a").attr("href").replace("?utm_source=thematic1", "")
            model.title = element.select("a").attr("title")
            model.imgUrl = element.select("img").attr("src")
            cache.add(model)
        }
        logger("getDetailsMoreNewsJ size cache ${cache.size}")
        return cache
    }

    // В хорошую погоду возращает айди(id) next/prev page
    fun getDetailsNavigation(html: String) : String? {
        val pattern = RegexStorage.News.Details.getNavigationPattern().toRegex()
        return pattern.matchEntire(html)?.groups?.get(1)?.value
    }

    // Да простит меня Бог Андроида.
    // И даст сил переписать это на человеческий лад с регулярками и куртизанками.
    fun getComments(source: String) : ArrayList<Comment> {
        val cache = ArrayList<Comment>()
        val doc: Document = Jsoup.parse(source)
        val element = doc.body()

        (0 until element.children().size)
                .asSequence()
                .map { element.child(it) }
                .forEach {
                    var nickname = ""
                    var userUrl = ""
                    var commentId = ""
                    var commentDate = ""
                    var commentText = ""
                    var commentReplay = ""
                    when {
                        it.tagName().contains("div") -> (0 until it.children().size)
                                .asSequence()
                                .map { j -> it.child(j) }
                                .forEach { ee ->
                                    if (ee.getElementsByClass("heading") != null) {
                                        val heading = it.getElementsByClass("heading").first()
                                        userUrl = heading.getElementsByClass("nickname").first().attr("href")
                                        nickname = heading.getElementsByClass("nickname").first().text()
                                        commentDate = heading.getElementsByClass("h-meta").first().text()
                                        commentId = heading.getElementsByClass("h-meta").first().select("a").attr("date-report-comment")
                                    }
                                    else if (ee.tagName().contains("p")) commentText = ee.html()
                                }
                        it.tagName().contains("ul") -> if (it.children().isNotEmpty()) {
                            commentReplay = it.toString()
                        }
                    }
                    cache.add(Comment(nickname, userUrl, commentId, commentDate, commentText, commentReplay))
                }
        logger("getComments size ${cache.size}")
        return cache
    }
}