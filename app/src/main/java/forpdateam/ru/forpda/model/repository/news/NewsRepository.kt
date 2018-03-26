package forpdateam.ru.forpda.model.repository.news

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.api.Api
import forpdateam.ru.forpda.api.ApiUtils
import forpdateam.ru.forpda.api.mentions.Mentions
import forpdateam.ru.forpda.api.mentions.models.MentionsData
import forpdateam.ru.forpda.api.news.NewsApi
import forpdateam.ru.forpda.api.news.models.Comment
import forpdateam.ru.forpda.api.news.models.DetailsPage
import forpdateam.ru.forpda.api.news.models.NewsItem
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Observable

/**
 * Created by radiationx on 01.01.18.
 */

class NewsRepository(
        private val schedulers: SchedulersProvider,
        private val newsApi: NewsApi
) {

    fun getNews(category: String, pageNumber: Int): Observable<List<NewsItem>> = Observable
            .fromCallable { newsApi.getNews(category, pageNumber) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun likeComment(articleId: Int, commentId: Int): Observable<Boolean> = Observable
            .fromCallable { newsApi.likeComment(articleId, commentId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendPoll(from: String, pollId: Int, answersId: IntArray): Observable<DetailsPage> = Observable
            .fromCallable { newsApi.sendPoll(from, pollId, answersId) }
            .map { transform(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun replyComment(article: DetailsPage, commentId: Int, comment: String): Observable<Comment> = Observable
            .fromCallable { newsApi.replyComment(article, commentId, comment) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getDetails(id: Int): Observable<DetailsPage> = Observable
            .fromCallable { newsApi.getDetails(id) }
            .map { transform(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getDetails(url: String): Observable<DetailsPage> = Observable
            .fromCallable { newsApi.getDetails(url) }
            .map { transform(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    private fun transform(page: DetailsPage): DetailsPage {
        val t = App.get().getTemplate(App.TEMPLATE_NEWS)
        App.setTemplateResStrings(t)
        t!!.setVariableOpt("style_type", App.get().cssStyleType)
        t.setVariableOpt("details_title", ApiUtils.htmlEncode(page.title))
        t.setVariableOpt("details_content", page.html)
        for (material in page.materials) {
            t.setVariableOpt("material_id", material.id)
            t.setVariableOpt("material_image", material.imageUrl)
            t.setVariableOpt("material_title", material.title)
            t.addBlockOpt("material")
        }
        page.html = t.generateOutput()
        t.reset()

        return page
    }

}
