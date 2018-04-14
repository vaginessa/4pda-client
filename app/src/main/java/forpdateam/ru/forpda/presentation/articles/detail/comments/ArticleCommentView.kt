package forpdateam.ru.forpda.presentation.articles.detail.comments

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage

/**
 * Created by radiationx on 01.01.18.
 */

interface ArticleCommentView : IBaseView {
    fun onReplyComment()
    fun showComments(comments: List<Comment>)
    fun setSendRefreshing(isRefreshing: Boolean)
    fun scrollToComment(position: Int)
}
