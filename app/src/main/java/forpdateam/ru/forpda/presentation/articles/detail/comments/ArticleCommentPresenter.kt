package forpdateam.ru.forpda.presentation.articles.detail.comments

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor
import java.util.*

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticleCommentPresenter(
        private val articleInteractor: ArticleInteractor
) : BasePresenter<ArticleCommentView>() {

    private var firstShow: Boolean = true

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        articleInteractor
                .observeComments()
                .map { commentsToList(it) }
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showComments(it)
                    if (firstShow) {
                        val targetCommentId = articleInteractor.initData.commentId
                        val index = it.indexOfFirst { it.id == targetCommentId }
                        viewState.scrollToComment(index)
                        firstShow = false
                    }
                })
                .addToDisposable()
    }

    fun updateComments() {
        articleInteractor
                .loadArticle()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun replyComment(commentId: Int, text: String) {
        articleInteractor
                .replyComment(commentId, text)
                .doOnTerminate { viewState.setSendRefreshing(true) }
                .doAfterTerminate { viewState.setSendRefreshing(false) }
                .subscribe({

                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun likeComment(commentId: Int) {
        articleInteractor
                .likeComment(commentId)
                .subscribe({

                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun commentsToList(comment: Comment): ArrayList<Comment> {
        val comments = ArrayList<Comment>()
        recurseCommentsToList(comments, comment)
        return comments
    }


    fun recurseCommentsToList(comments: ArrayList<Comment>, comment: Comment) {
        for (child in comment.children) {
            comments.add(Comment(child))
            recurseCommentsToList(comments, child)
        }
    }


}
