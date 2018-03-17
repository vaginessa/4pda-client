package forpdateam.ru.forpda.presentation.theme

import forpdateam.ru.forpda.api.IBaseForumPost
import forpdateam.ru.forpda.api.theme.models.ThemePage
import forpdateam.ru.forpda.common.mvp.IBaseView

/**
 * Created by radiationx on 15.03.18.
 */

interface ThemeView : IBaseView {
    fun findNext(next: Boolean)
    fun findText(text: String)
    fun updateShowAvatarState(isShow: Boolean)
    fun updateTypeAvatarState(isCircle: Boolean)
    fun setFontSize(size: Int)
    fun scrollToAnchor(anchor: String?)
    fun updateHistoryLastHtml()

    fun onLoadData(newPage: ThemePage)
    fun updateView(page: ThemePage)

    fun setMessageRefreshing(isRefreshing: Boolean)
    fun onMessageSent()

    fun showDeleteInFavDialog(page: ThemePage)
    fun showAddInFavDialog(page: ThemePage)


    fun firstPage()
    fun prevPage()
    fun nextPage()
    fun lastPage()
    fun selectPage()

    fun deletePostUi(post: IBaseForumPost)
    fun showUserMenu(post: IBaseForumPost)
    fun showReputationMenu(post: IBaseForumPost)
    fun showPostMenu(post: IBaseForumPost)
    fun reportPost(post: IBaseForumPost)
    fun reply(post: IBaseForumPost)
    fun quotePost(text: String, post: IBaseForumPost)
    fun deletePost(post: IBaseForumPost)
    fun editPost(post: IBaseForumPost)
    fun votePost(post: IBaseForumPost, type: Boolean)
    fun changeReputation(post: IBaseForumPost, type: Boolean)

    fun openAnchorDialog(post: IBaseForumPost, anchorName: String)
    fun openSpoilerLinkDialog(post: IBaseForumPost, spoilNumber: String)

    fun toast(text: String)
    fun log(text: String)
}
