package forpdateam.ru.forpda.model.repository.news

import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.news.NewsApi
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
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun replyComment(articleId: Int, commentId: Int, comment: String): Observable<DetailsPage> = Observable
            .fromCallable { newsApi.replyComment(articleId, commentId, comment) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getDetails(id: Int): Observable<DetailsPage> = Observable
            .fromCallable { newsApi.getDetails(id) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getDetails(url: String): Observable<DetailsPage> = Observable
            .fromCallable { newsApi.getDetails(url) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getComments(article: DetailsPage): Observable<Comment> = Observable
            .fromCallable { newsApi.parseComments(article.karmaMap, article.commentsSource) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
