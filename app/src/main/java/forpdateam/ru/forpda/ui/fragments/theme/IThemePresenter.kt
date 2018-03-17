package forpdateam.ru.forpda.ui.fragments.theme

/**
 * Created by radiationx on 17.03.18.
 */
interface IThemePresenter {
    fun firstPage()
    fun prevPage()
    fun nextPage()
    fun lastPage()
    fun selectPage()
    fun showUserMenu(postId: String)
    fun showReputationMenu(postId: String)
    fun showPostMenu(postId: String)
    fun reportPost(postId: String)
    fun reply(postId: String)
    fun quotePost(text: String, postId: String)
    fun deletePost(postId: String)
    fun editPost(postId: String)
    fun votePost(postId: String, type: Boolean)
    fun setHistoryBody(index: String, body: String)
    fun copySelectedText(text: String)
    fun toast(text: String)
    fun log(text: String)
    fun showPollResults()
    fun showPoll()
    fun copySpoilerLink(postId: String, spoilNumber: String)
    fun shareSelectedText(text: String)
    fun anchorDialog(postId: String, name: String)
    fun setPollOpen(bValue: String)
    fun setHatOpen(bValue: String)
}