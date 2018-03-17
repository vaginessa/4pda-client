package forpdateam.ru.forpda.presentation.theme

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.api.ApiUtils
import forpdateam.ru.forpda.api.IBaseForumPost
import forpdateam.ru.forpda.api.theme.Theme
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm
import forpdateam.ru.forpda.api.theme.models.ThemePage
import forpdateam.ru.forpda.apirx.RxApi
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.Utils

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.theme.ThemeRepository
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity
import forpdateam.ru.forpda.ui.fragments.theme.IThemePresenter
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb
import forpdateam.ru.forpda.ui.fragments.theme.editpost.EditPostFragment
import org.acra.ACRA
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern

/**
 * Created by radiationx on 15.03.18.
 */
@InjectViewState
class ThemePresenter(
        private val themeRepository: ThemeRepository
) : BasePresenter<ThemeView>(), IThemePresenter {


    var loadAction = ActionState.NORMAL
    var currentPage: ThemePage? = null
    var history = mutableListOf<ThemePage>()
    var themeUrl: String = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadUrl(themeUrl)
    }

    fun getPageScrollY() = currentPage?.scrollY ?: 0

    fun canQuote() = currentPage?.canQuote() ?: false

    fun isPageLoaded() = currentPage != null

    fun isInFavorites() = currentPage?.isInFavorite ?: false

    fun getId() = currentPage?.id ?: -1

    private fun loadData(url: String, action: ActionState) {
        var hatOpen = false
        var pollOpen = false
        currentPage?.let {
            hatOpen = it.isHatOpen
            pollOpen = it.isPollOpen
        }
        themeUrl = url
        loadAction = action
        viewState.updateHistoryLastHtml()
        themeRepository
                .getTheme(url, true, hatOpen, pollOpen)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    onLoadData(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    private fun onLoadData(page: ThemePage) {
        currentPage = page
        viewState.onLoadData(page)
        if (loadAction === ActionState.NORMAL) {
            saveToHistory(page)
        }
        if (loadAction === ActionState.REFRESH) {
            updateHistoryLast(page)
        }
    }

    private fun createEditPostForm(message: String, attachments: MutableList<AttachmentItem>): EditPostForm? = currentPage?.let {
        val form = EditPostForm()
        form.forumId = it.forumId
        form.topicId = it.id
        form.st = it.pagination.current * it.pagination.perPage
        form.message = message
        form.attachments.addAll(attachments)
        form
    }

    fun openEditPostForm(message: String, attachments: MutableList<AttachmentItem>) {
        currentPage?.let { page ->
            createEditPostForm(message, attachments)?.let {
                TabManager.get().add(EditPostFragment.newInstance(it, page.title))
            }
        }
    }

    fun openEditPostForm(postId: Int) {
        currentPage?.let {
            TabManager.get().add(EditPostFragment.newInstance(postId, it.id, it.forumId, it.st, it.title))
        }
    }


    fun sendMessage(message: String, attachments: MutableList<AttachmentItem>) {
        createEditPostForm(message, attachments)?.let {
            viewState.setMessageRefreshing(true)
            themeRepository
                    .sendPost(it)
                    .doOnTerminate { viewState.setMessageRefreshing(true) }
                    .doAfterTerminate { viewState.setMessageRefreshing(false) }
                    .subscribe({
                        onLoadData(it)
                        viewState.onMessageSent()
                    }, {
                        this.handleErrorRx(it)
                    })
                    .addToDisposable()
        }
    }

    fun loadUrl(url: String) {
        loadData(url, ActionState.NORMAL)
    }

    fun reload() {
        loadData(themeUrl, ActionState.REFRESH)
    }

    fun loadNewPosts() {
        currentPage?.let {
            loadUrl("https://4pda.ru/forum/index.php?showtopic=${it.id}&view=getnewpost")
        }
    }

    fun loadPage(page: Int) {
        currentPage?.let {
            var url = "https://4pda.ru/forum/index.php?showtopic=${it.id}"
            if (page != 0) {
                url = "$url&st=$page"
            }
            loadUrl(url)
        }
    }

    fun backPage() {
        if (history.size > 1) {
            loadAction = ActionState.BACK
            history.removeAt(history.size - 1)
            history.last().let {
                currentPage = it
                themeUrl = it.url
                viewState.updateView(it)
            }
        }
    }

    override fun showPollResults() {
        val url = themeUrl
                .replaceFirst("#[^&]*", "")
                .replace("&mode=show", "")
                .replace("&poll_open=true", "") + "&mode=show&poll_open=true"
        loadUrl(url)
    }

    override fun showPoll() {
        val url = themeUrl
                .replaceFirst("#[^&]*", "")
                .replace("&mode=show", "")
                .replace("&poll_open=true", "") + "&poll_open=true"
        loadUrl(url)
    }

    private fun saveToHistory(themePage: ThemePage) {
        history.add(themePage)
    }

    private fun updateHistoryLast(themePage: ThemePage) {
        if (history.isNotEmpty()) {
            history.last().let {
                themePage.anchors.addAll(it.anchors)
                themePage.scrollY = it.scrollY
            }
            history[history.size - 1] = themePage
        }
    }

    fun updateHistoryLastHtml(html: String, scrollY: Int) {
        if (history.isNotEmpty()) {
            history.last().let {
                it.scrollY = scrollY
                it.html = html
            }
        }
    }

    override fun shareSelectedText(text: String) {
        Utils.shareText(text)
    }

    fun copyLink() {
        currentPage?.let {
            Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showtopic=${it.id}")
        }
    }

    fun openSearch() {
        currentPage?.let {
            IntentHandler.handle("https://4pda.ru/forum/index.php?forums=${it.forumId}&topics=${it.id}&act=search&source=pst&result=posts")
        }
    }

    fun openSearchMyPosts() {
        currentPage?.let {
            var url = ("https://4pda.ru/forum/index.php?forums=${it.forumId}&topics=${it.id}&act=search&source=pst&result=posts&username=")

            try {
                url += URLEncoder.encode(App.get().preferences.getString("auth.user.nick", "null"), "windows-1251")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            IntentHandler.handle(url)
        }
    }

    fun openForum() {
        currentPage?.let {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=${it.forumId}")
        }
    }


    private fun getPostById(postId: Int): IBaseForumPost? = currentPage
            ?.posts
            ?.firstOrNull {
                it.id == postId
            }

    override fun firstPage() = viewState.firstPage()

    override fun prevPage() = viewState.prevPage()

    override fun nextPage() = viewState.nextPage()

    override fun lastPage() = viewState.lastPage()

    override fun selectPage() = viewState.selectPage()


    override fun showUserMenu(postId: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.showUserMenu(it)
        }
    }

    override fun showReputationMenu(postId: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.showReputationMenu(it)
        }
    }

    override fun showPostMenu(postId: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.showPostMenu(it)
        }
    }

    override fun reportPost(postId: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.reportPost(it)
        }
    }

    override fun reply(postId: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.reply(it)
        }
    }


    override fun quotePost(text: String, postId: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.quotePost(text, it)
        }
    }

    override fun deletePost(postId: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.deletePost(it)
        }
    }

    override fun editPost(postId: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.editPost(it)
        }
    }

    override fun votePost(postId: String, type: Boolean) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.votePost(it, type)
        }
    }

    override fun setHistoryBody(index: String, body: String) {
        history[Integer.parseInt(index)].html = body
    }

    override fun copySelectedText(text: String) {
        Utils.copyToClipBoard(text)
    }

    override fun toast(text: String) {
        viewState.toast(text)
    }

    override fun log(text: String) {
        viewState.log(text)
    }

    override fun copySpoilerLink(postId: String, spoilNumber: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.openSpoilerLinkDialog(it, spoilNumber)
        }
    }

    override fun anchorDialog(postId: String, name: String) {
        getPostById(Integer.parseInt(postId))?.let {
            viewState.openAnchorDialog(it, name)
        }
    }

    override fun setPollOpen(bValue: String) {
        currentPage?.let {
            it.isPollOpen = bValue.toBoolean()
        }
    }

    override fun setHatOpen(bValue: String) {
        currentPage?.let {
            it.isHatOpen = bValue.toBoolean()
        }
    }


    private val LOG_TAG = ThemeFragmentWeb::class.java.simpleName
    fun handleNewUrl(uri: Uri) {
        Log.d(LOG_TAG, "handle $uri")
        val url = uri.toString()
        try {
            if (checkIsPoll(url)) {
                return
            }
            if (uri.host != null && uri.host.matches("4pda.ru".toRegex())) {
                if (uri.pathSegments[0] == "forum") {
                    var param: String? = uri.getQueryParameter("showtopic")
                    Log.d(LOG_TAG, "param showtopic: $param")
                    if (param != null && param != Uri.parse(themeUrl).getQueryParameter("showtopic")) {
                        loadUrl(url)
                        return
                    }
                    param = uri.getQueryParameter("act")
                    if (param == null)
                        param = uri.getQueryParameter("view")
                    Log.d(LOG_TAG, "param act|view: $param")
                    if (param != null && param == "findpost") {
                        var postId: String? = uri.getQueryParameter("pid")
                        if (postId == null)
                            postId = uri.getQueryParameter("p")
                        Log.d(LOG_TAG, "param pid|p: $postId")
                        if (postId != null) {
                            postId = postId.replace("[^\\d][\\s\\S]*?".toRegex(), "")
                        }
                        Log.d(LOG_TAG, "param postId: $postId")
                        if (postId != null && getPostById(Integer.parseInt(postId.trim { it <= ' ' })) != null) {
                            val matcher = Theme.elemToScrollPattern.matcher(url)
                            var elem: String? = null
                            while (matcher.find()) {
                                elem = matcher.group(1)
                            }
                            Log.d(LOG_TAG, " scroll to $postId : $elem")
                            val finalAnchor = (if (elem == null) "entry" else "") + if (elem != null) elem else postId
                            currentPage?.let {
                                if (App.get().preferences.getBoolean("theme.anchor_history", true)) {
                                    it.addAnchor(finalAnchor)
                                }
                            }

                            viewState.scrollToAnchor(finalAnchor)
                            return
                        } else {
                            loadUrl(url)
                            return
                        }
                    }
                }
            }

            if (Theme.attachImagesPattern.matcher(url).find()) {
                currentPage?.let {
                    for (post in it.getPosts()) {
                        for (image in post.attachImages) {
                            if (image.first.contains(url)) {
                                val list = ArrayList<String>()
                                for (attaches in post.attachImages) {
                                    list.add(attaches.first)
                                }
                                ImageViewerActivity.startActivity(App.getContext(), list, post.attachImages.indexOf(image))
                                return
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ACRA.getErrorReporter().handleException(ex)
        }
        IntentHandler.handle(url)
    }

    private fun checkIsPoll(url: String): Boolean {
        currentPage?.let {
            val m = Pattern.compile("4pda.ru.*?addpoll=1").matcher(url)
            if (m.find()) {
                var uri = Uri.parse(url)
                uri = uri.buildUpon()
                        .appendQueryParameter("showtopic", Integer.toString(it.id))
                        .appendQueryParameter("st", "" + it.pagination.current * it.pagination.perPage)
                        .build()
                loadUrl(uri.toString())
                return true
            }
        }
        return false
    }


    fun onClickDeleteInFav() {
        currentPage?.let { viewState.showDeleteInFavDialog(it) }
    }

    fun onClickAddInFav() {
        currentPage?.let { viewState.showAddInFavDialog(it) }
    }

    fun onBackPressed(): Boolean {
        if (App.get().preferences.getBoolean("theme.anchor_history", true)) {
            currentPage?.let {
                if (it.anchors.size > 1) {
                    it.removeAnchor()
                    viewState.scrollToAnchor(it.anchor)
                    return true
                }
            }
        }
        if (history.size > 1) {
            backPage()
            return true
        }
        return false
    }

    enum class ActionState(private val id: Int) {
        BACK(0),
        REFRESH(2),
        NORMAL(2);

        override fun toString() = id.toString()
    }
}