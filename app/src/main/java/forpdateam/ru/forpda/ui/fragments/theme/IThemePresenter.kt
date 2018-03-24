package forpdateam.ru.forpda.ui.fragments.theme

/**
 * Created by radiationx on 17.03.18.
 */
interface IThemePresenter {
    fun onFirstPageClick()
    fun onPrevPageClick()
    fun onNextPageClick()
    fun onLastPageClick()
    fun onSelectPageClick()

    fun onUserMenuClick(postId: Int)
    fun onReputationMenuClick(postId: Int)
    fun onPostMenuClick(postId: Int)

    fun onReportPostClick(postId: Int)
    fun onReplyPostClick(postId: Int)
    fun onQuotePostClick(postId: Int, text: String)
    fun onDeletePostClick(postId: Int)
    fun onEditPostClick(postId: Int)
    fun onVotePostClick(postId: Int, type: Boolean)

    fun setHistoryBody(index: Int, body: String)

    fun copyText(text: String)
    fun shareText(text: String)
    fun toast(text: String)
    fun log(text: String)

    fun onPollResultsClick()
    fun onPollClick()

    fun onSpoilerCopyLinkClick(postId: Int, spoilNumber: String)
    fun onAnchorClick(postId: Int, name: String)

    fun onPollHeaderClick(bValue: Boolean)
    fun onHatHeaderClick(bValue: Boolean)
}