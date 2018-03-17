package forpdateam.ru.forpda.ui.fragments.theme

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

/**
 * Created by radiationx on 17.03.18.
 */
class ThemeJsInterface(
        private val presenter: IThemePresenter
) {

    private val handler = Handler(Looper.getMainLooper())

    private fun runInUiThread(runnable: Runnable) {
        handler.post(runnable)
    }

    @JavascriptInterface
    fun firstPage() {
        runInUiThread(Runnable { presenter.firstPage() })
    }

    @JavascriptInterface
    fun prevPage() {
        runInUiThread(Runnable { presenter.prevPage() })
    }

    @JavascriptInterface
    fun nextPage() {
        runInUiThread(Runnable { presenter.nextPage() })
    }

    @JavascriptInterface
    fun lastPage() {
        runInUiThread(Runnable { presenter.lastPage() })
    }

    @JavascriptInterface
    fun selectPage() {
        runInUiThread(Runnable { presenter.selectPage() })
    }

    @JavascriptInterface
    fun showUserMenu(postId: String) {
        runInUiThread(Runnable { presenter.showUserMenu(postId) })
    }

    @JavascriptInterface
    fun showReputationMenu(postId: String) {
        runInUiThread(Runnable { presenter.showReputationMenu(postId) })
    }

    @JavascriptInterface
    fun showPostMenu(postId: String) {
        runInUiThread(Runnable { presenter.showPostMenu(postId) })
    }

    @JavascriptInterface
    fun reportPost(postId: String) {
        runInUiThread(Runnable { presenter.reportPost(postId) })
    }

    @JavascriptInterface
    fun reply(postId: String) {
        runInUiThread(Runnable { presenter.reply(postId) })
    }

    @JavascriptInterface
    fun quotePost(text: String, postId: String) {
        runInUiThread(Runnable { presenter.quotePost(text, postId) })
    }

    @JavascriptInterface
    fun deletePost(postId: String) {
        runInUiThread(Runnable { presenter.deletePost(postId) })
    }

    @JavascriptInterface
    fun editPost(postId: String) {
        runInUiThread(Runnable { presenter.editPost(postId) })
    }

    @JavascriptInterface
    fun votePost(postId: String, type: Boolean) {
        runInUiThread(Runnable { presenter.votePost(postId, type) })
    }

    @JavascriptInterface
    fun setHistoryBody(index: String, body: String) {
        runInUiThread(Runnable { presenter.setHistoryBody(index, body) })
    }

    @JavascriptInterface
    fun copySelectedText(text: String) {
        runInUiThread(Runnable { presenter.copySelectedText(text) })
    }

    @JavascriptInterface
    fun toast(text: String) {
        runInUiThread(Runnable { presenter.toast(text) })
    }

    @JavascriptInterface
    fun log(text: String) {
        runInUiThread(Runnable { presenter.log(text) })
    }

    @JavascriptInterface
    fun showPollResults() {
        runInUiThread(Runnable { presenter.showPollResults() })
    }

    @JavascriptInterface
    fun showPoll() {
        runInUiThread(Runnable { presenter.showPoll() })
    }

    @JavascriptInterface
    fun copySpoilerLink(postId: String, spoilNumber: String) {
        runInUiThread(Runnable { presenter.copySpoilerLink(postId, spoilNumber) })
    }

    @JavascriptInterface
    fun setPollOpen(bValue: String) {
        runInUiThread(Runnable { presenter.setPollOpen(bValue) })
    }

    @JavascriptInterface
    fun setHatOpen(bValue: String) {
        runInUiThread(Runnable { presenter.setHatOpen(bValue) })
    }

    @JavascriptInterface
    fun shareSelectedText(text: String) {
        runInUiThread(Runnable { presenter.shareSelectedText(text) })
    }

    @JavascriptInterface
    fun anchorDialog(postId: String, name: String) {
        runInUiThread(Runnable { presenter.anchorDialog(postId, name) })
    }
}