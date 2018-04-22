package forpdateam.ru.forpda.presentation.search

import android.net.Uri
import android.util.Log
import android.util.Pair
import android.widget.Spinner
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.IBaseForumPost
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.search.SearchRepository
import forpdateam.ru.forpda.presentation.theme.IThemePresenter
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb
import org.acra.ACRA
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern

@InjectViewState
class SearchPresenter(
        private val searchRepository: SearchRepository,
        private val favoritesRepository: FavoritesRepository
) : BasePresenter<SearchSiteView>(), IThemePresenter {

    companion object {
        const val FIELD_RESOURCE = "resource"
        const val FIELD_RESULT = "result"
        const val FIELD_SORT = "sort"
        const val FIELD_SOURCE = "source"
    }

    private val resourceItems = listOf<String>(SearchSettings.RESOURCE_FORUM.second, SearchSettings.RESOURCE_NEWS.second)
    private val resultItems = listOf<String>(SearchSettings.RESULT_TOPICS.second, SearchSettings.RESULT_POSTS.second)
    private val sortItems = listOf<String>(SearchSettings.SORT_DA.second, SearchSettings.SORT_DD.second, SearchSettings.SORT_REL.second)
    private val sourceItems = listOf<String>(SearchSettings.SOURCE_ALL.second, SearchSettings.SOURCE_TITLES.second, SearchSettings.SOURCE_CONTENT.second)

    private val fields = mapOf(
            FIELD_RESOURCE to resourceItems,
            FIELD_RESULT to resultItems,
            FIELD_SORT to sortItems,
            FIELD_SOURCE to sourceItems
    )

    private var settings = SearchSettings()

    fun initSearchSettings(url: String?) {
        url?.let {
            settings = SearchSettings.parseSettings(settings, it)
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.fillSettingsData(settings, fields)
    }

    fun refreshData() {
        if (settings.query.isEmpty() && settings.nick.isEmpty()) {
            return
        }
        val withHtml = settings.result == SearchSettings.RESULT_POSTS.first && settings.resourceType.equals(SearchSettings.RESOURCE_FORUM.first)
        searchRepository
                .getSearch(settings, withHtml)
                .doOnTerminate {
                    viewState.setRefreshing(true)
                    viewState.onStartSearch(settings)
                }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showData(it)
                }, {
                    it.printStackTrace()
                })
    }

    fun search(query: String, nick: String) {
        settings.st = 0
        settings.query = query
        settings.nick = nick
        refreshData()
    }

    fun search(pageNumber: Int) {
        settings.st = pageNumber
        refreshData()
    }

    fun updateSettings(field: String, position: Int) {
        when (field) {
            FIELD_RESOURCE -> {
                val name = resourceItems[position]
                when {
                    checkName(name, SearchSettings.RESOURCE_NEWS) -> {
                        settings.resourceType = SearchSettings.RESOURCE_NEWS.first
                        viewState.setNewsMode()
                    }
                    checkName(name, SearchSettings.RESOURCE_FORUM) -> {
                        settings.resourceType = SearchSettings.RESOURCE_FORUM.first
                        viewState.setForumMode()
                    }
                }
            }
            FIELD_RESULT -> {
                val name = resultItems[position]
                when {
                    checkName(name, SearchSettings.RESULT_TOPICS) -> settings.result = SearchSettings.RESULT_TOPICS.first
                    checkName(name, SearchSettings.RESULT_POSTS) -> settings.result = SearchSettings.RESULT_POSTS.first
                }
            }
            FIELD_SORT -> {
                val name = sortItems[position]
                when {
                    checkName(name, SearchSettings.SORT_DA) -> settings.sort = SearchSettings.SORT_DA.first
                    checkName(name, SearchSettings.SORT_DD) -> settings.sort = SearchSettings.SORT_DD.first
                    checkName(name, SearchSettings.SORT_REL) -> settings.sort = SearchSettings.SORT_REL.first
                }
            }
            FIELD_SOURCE -> {
                val name = sourceItems[position]
                when {
                    checkName(name, SearchSettings.SOURCE_ALL) -> settings.source = SearchSettings.SOURCE_ALL.first
                    checkName(name, SearchSettings.SOURCE_TITLES) -> settings.source = SearchSettings.SOURCE_TITLES.first
                    checkName(name, SearchSettings.SOURCE_CONTENT) -> settings.source = SearchSettings.SOURCE_CONTENT.first
                }
            }
        }
    }

    private fun checkName(arg: String, pair: Pair<String, String>): Boolean {
        return arg == pair.second
    }


    fun saveSettings() {
        val saveSettings = SearchSettings()
        saveSettings.result = settings.result
        saveSettings.sort = settings.sort
        saveSettings.source = settings.source
        val saveUrl = saveSettings.toUrl()
        App.get().preferences.edit().putString("search_settings", saveUrl).apply()
    }

    fun onItemClick(item: SearchItem) {
        var url = ""
        if (settings.resourceType.equals(SearchSettings.RESOURCE_NEWS.first)) {
            url = "https://4pda.ru/index.php?p=${item.id}"
        } else {
            url = "https://4pda.ru/forum/index.php?showtopic=${item.topicId}"
            if (item.id != 0) {
                url += "&view=findpost&p=${item.id}"
            }
        }
        IntentHandler.handle(url)
    }

    fun onItemLongClick(item: SearchItem) {
        viewState.showItemDialogMenu(item, settings)
    }

    fun copyLink() {
        Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showtopic=${settings.toUrl()}")
    }

    fun copyLink(item: IBaseForumPost) {
        var url = ""
        if (settings.resourceType.equals(SearchSettings.RESOURCE_NEWS.first)) {
            url = "https://4pda.ru/index.php?p=${item.id}"
        } else {
            url = "https://4pda.ru/forum/index.php?showtopic=${item.topicId}"
            if (item.id != 0) {
                url += "&view=findpost&p=${item.id}"
            }
        }
        Utils.copyToClipBoard(url)
    }

    fun openTopicBegin(item: IBaseForumPost) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=${item.topicId}")
    }

    fun openTopicNew(item: IBaseForumPost) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=${item.topicId}&view=getnewpost")
    }

    fun openTopicLast(item: IBaseForumPost) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=${item.topicId}&view=getlastpost")
    }

    fun openForum(item: IBaseForumPost) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=${item.forumId}")
    }

    fun onClickAddInFav(item: IBaseForumPost) {
        viewState.showAddInFavDialog(item)
    }

    fun addTopicToFavorite(topicId: Int, subType: String) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_ADD, -1, topicId, subType)
                .subscribe({
                    viewState.onAddToFavorite(it)
                }, {
                    it.printStackTrace()
                })
    }

    /* ITHEME PReSNETER*/

    override fun onPollResultsClick() {
        val url = themeUrl
                .replaceFirst("#[^&]*", "")
                .replace("&mode=show", "")
                .replace("&poll_open=true", "") + "&mode=show&poll_open=true"
        loadUrl(url)
    }

    override fun onPollClick() {
        val url = themeUrl
                .replaceFirst("#[^&]*", "")
                .replace("&mode=show", "")
                .replace("&poll_open=true", "") + "&poll_open=true"
        loadUrl(url)
    }


    override fun shareText(text: String) {
        Utils.shareText(text)
    }


    private fun getPostById(postId: Int): IBaseForumPost? = currentPage
            ?.posts
            ?.firstOrNull {
                it.id == postId
            }

    override fun onFirstPageClick() = viewState.firstPage()

    override fun onPrevPageClick() = viewState.prevPage()

    override fun onNextPageClick() = viewState.nextPage()

    override fun onLastPageClick() = viewState.lastPage()

    override fun onSelectPageClick() = viewState.selectPage()

    override fun onUserMenuClick(postId: Int) {
        getPostById(postId)?.let { viewState.showUserMenu(it) }
    }

    override fun onReputationMenuClick(postId: Int) {
        getPostById(postId)?.let { viewState.showReputationMenu(it) }
    }

    override fun onPostMenuClick(postId: Int) {
        getPostById(postId)?.let { viewState.showPostMenu(it) }
    }

    override fun onReportPostClick(postId: Int) {
        getPostById(postId)?.let { viewState.reportPost(it) }
    }

    override fun onReplyPostClick(postId: Int) {
        getPostById(postId)?.let {
            val text = "[snapback]${it.id}[/snapback] [b]${it.nick},[/b] \n"
            viewState.insertText(text)
        }
    }

    override fun onQuotePostClick(postId: Int, text: String) {
        getPostById(postId)?.let {
            val date = Utils.getForumDateTime(Utils.parseForumDateTime(it.getDate()))
            val insert = "[quote name=\"${it.nick}\" date=\"$date\" post=${it.id}]$text[/quote]\n"
            viewState.insertText(insert)
        }
    }

    override fun onDeletePostClick(postId: Int) {
        getPostById(postId)?.let { viewState.deletePost(it) }
    }

    override fun onEditPostClick(postId: Int) {
        getPostById(postId)?.let { viewState.editPost(it) }
    }

    override fun onVotePostClick(postId: Int, type: Boolean) {
        getPostById(postId)?.let { viewState.votePost(it, type) }
    }

    override fun onSpoilerCopyLinkClick(postId: Int, spoilNumber: String) {
        getPostById(postId)?.let { viewState.openSpoilerLinkDialog(it, spoilNumber) }
    }

    override fun onAnchorClick(postId: Int, name: String) {
        getPostById(postId)?.let { viewState.openAnchorDialog(it, name) }
    }

    override fun onPollHeaderClick(bValue: Boolean) {
        currentPage?.let { it.isPollOpen = bValue }
    }

    override fun onHatHeaderClick(bValue: Boolean) {
        currentPage?.let { it.isHatOpen = bValue }
    }

    override fun setHistoryBody(index: Int, body: String) {
        history[index].html = body
    }

    override fun copyText(text: String) {
        Utils.copyToClipBoard(text)
    }

    override fun toast(text: String) {
        viewState.toast(text)
    }

    override fun log(text: String) {
        viewState.log(text)
    }

    override fun openProfile(postId: Int) {
        getPostById(postId)?.let {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=${it.userId}")
        }
    }

    override fun openQms(postId: Int) {
        getPostById(postId)?.let {
            IntentHandler.handle("https://4pda.ru/forum/index.php?act=qms&amp;mid=${it.userId}")
        }
    }

    override fun openSearchUserTopic(postId: Int) {
        getPostById(postId)?.let {
            IntentHandler.handle(SearchSettings().apply {
                source = SearchSettings.SOURCE_ALL.first
                nick = it.nick
                result = SearchSettings.RESULT_TOPICS.first
            }.toUrl())
        }
    }

    override fun openSearchInTopic(postId: Int) {
        getPostById(postId)?.let {
            IntentHandler.handle(SearchSettings().apply {
                addForum(Integer.toString(it.forumId))
                addTopic(Integer.toString(it.topicId))
                source = SearchSettings.SOURCE_CONTENT.first
                nick = it.nick
                result = SearchSettings.RESULT_POSTS.first
                subforums = SearchSettings.SUB_FORUMS_FALSE
            }.toUrl())
        }
    }

    override fun openSearchUserMessages(postId: Int) {
        getPostById(postId)?.let {
            IntentHandler.handle(SearchSettings().apply {
                source = SearchSettings.SOURCE_CONTENT.first
                nick = it.getNick()
                result = SearchSettings.RESULT_POSTS.first
                subforums = SearchSettings.SUB_FORUMS_FALSE
            }.toUrl())
        }
    }

    override fun onChangeReputationClick(postId: Int, type: Boolean) {
        getPostById(postId)?.let { viewState.showChangeReputation(it, type) }
    }

    override fun changeReputation(postId: Int, type: Boolean, message: String) {
        getPostById(postId)?.let {
            reputationRepository
                    .changeReputation(it.id, it.userId, type, message)
                    .subscribe({
                        toast(App.get().getString(R.string.reputation_changed))
                    }, {
                        this.handleErrorRx(it)
                    })
                    .addToDisposable()
        }
    }

    override fun votePost(postId: Int, type: Boolean) {
        getPostById(postId)?.let {
            themeRepository
                    .votePost(it.id, type)
                    .subscribe({
                        toast(it)
                    }, {
                        this.handleErrorRx(it)
                    })
                    .addToDisposable()
        }
    }

    override fun openReputationHistory(postId: Int) {
        getPostById(postId)?.let {
            IntentHandler.handle("https://4pda.ru/forum/index.php?act=rep&view=history&amp;mid=${it.userId}")
        }
    }

    override fun quoteFromBuffer(postId: Int) {
        getPostById(postId)?.let {
            val text = Utils.readFromClipboard()
            if (!text.isNullOrEmpty()) {
                onQuotePostClick(postId, text)
            }
        }
    }

    override fun reportPost(postId: Int, message: String) {
        getPostById(postId)?.let { post ->
            currentPage?.let {
                themeRepository
                        .reportPost(it.id, post.id, message)
                        .subscribe({
                            toast("Жалоба отправлена")
                        }, {
                            this.handleErrorRx(it)
                        })
                        .addToDisposable()
            }
        }
    }

    override fun deletePost(postId: Int) {
        getPostById(postId)?.let { post ->
            themeRepository
                    .deletePost(post.id)
                    .subscribe({
                        if (it) {
                            viewState.deletePostUi(post)
                        }
                        toast(App.get().getString(R.string.message_deleted))
                    }, {
                        this.handleErrorRx(it)
                    })
                    .addToDisposable()
        }
    }

    override fun createNote(postId: Int) {
        getPostById(postId)?.let {
            val themeTitle: String = currentPage?.title.orEmpty()
            val title = String.format(App.get().getString(R.string.post_Topic_Nick_Number), themeTitle, it.nick, it.id)
            val url = "https://4pda.ru/forum/index.php?s=&showtopic=" + it.topicId + "&view=findpost&p=" + it.id
            viewState.showNoteCreate(title, url)
        }
    }

    override fun copyPostLink(postId: Int) {
        getPostById(postId)?.let {
            val url = "https://4pda.ru/forum/index.php?s=&showtopic=${it.topicId}&view=findpost&p=${it.id}"
            copyText(url)
        }
    }

    override fun sharePostLink(postId: Int) {
        getPostById(postId)?.let {
            val url = "https://4pda.ru/forum/index.php?s=&showtopic=${it.topicId}&view=findpost&p=${it.id}"
            shareText(url)
        }
    }

    override fun copyAnchorLink(postId: Int, name: String) {
        getPostById(postId)?.let {
            val url = "https://4pda.ru/forum/index.php?act=findpost&pid=${it.id}&anchor=$name"
            copyText(url)
        }
    }

    override fun copySpoilerLink(postId: Int, spoilNumber: String) {
        getPostById(postId)?.let {
            val url = "https://4pda.ru/forum/index.php?act=findpost&pid=${it.id}&anchor=Spoil-${it.id}-$spoilNumber"
            copyText(url)
        }
    }
}