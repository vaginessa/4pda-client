package forpdateam.ru.forpda.presentation.articles.detail.content

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticleContentPresenter(
        private val articleInteractor: ArticleInteractor
) : BasePresenter<ArticleContentView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        articleInteractor
                .observeData()
                .subscribe({
                    viewState.showData(it)
                })
                .addToDisposable()
    }

    fun sendPoll(from: String, pollId: Int, answersId: IntArray) {
        articleInteractor
                .sendPoll(from, pollId, answersId)
                .subscribe()
                .addToDisposable()
    }

}
