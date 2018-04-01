package forpdateam.ru.forpda.presentation.articles.detail

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.model.repository.news.NewsRepository

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticleDetailPresenter(
        private val newsRepository: NewsRepository
) : BasePresenter<ArticleDetailView>() {

    var newsUrl: String? = null
    var newsId: Int = 0
    var commentId: Int = 0
    /*var newsTitle: String? = null
    var newsNick: String? = null
    var newsCount = -1
    var newsDate: String? = null
    var newsImageUrl: String? = null*/
    var currentData: DetailsPage? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadArticle()
    }

    fun loadArticle() {
        newsUrl
                ?.let {
                    newsRepository.getDetails(it)
                } ?: newsRepository.getDetails(newsId)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    it.commentId = commentId
                    viewState.showArticle(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun openAuthorProfile() {
        currentData?.let {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=${it.authorId}")
        }
    }

    fun copyLink() {
        currentData?.let {
            Utils.copyToClipBoard("https://4pda.ru/index.php?p=${it.id}")
        }
    }

    fun shareLink() {
        currentData?.let {
            Utils.shareText("https://4pda.ru/index.php?p=${it.id}")
        }
    }

    fun createNote() {
        currentData?.let {
            val url = "https://4pda.ru/index.php?p=${it.id}"
            viewState.showCreateNote(it.title, url)
        }
    }

}
