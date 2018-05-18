package forpdateam.ru.forpda.presentation.topics

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.model.repository.topics.TopicsRepository
import forpdateam.ru.forpda.presentation.IRouter
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.forum.ForumFragment
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
class TopicsPresenter(
        private val topicsRepository: TopicsRepository,
        private val forumRepository: ForumRepository,
        private val favoritesRepository: FavoritesRepository,
        private val router: IRouter
) : BasePresenter<TopicsView>() {

    var id = 0
    private var currentSt = 0
    var currentData: TopicsData? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadTopics()
    }

    fun loadTopics() {
        topicsRepository
                .getTopics(id, currentSt)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showTopics(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun loadPage(st: Int) {
        currentSt = st
        loadTopics()
    }

    fun addForumToFavorite(forumId: Int, subType: String) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_ADD_FORUM, -1, forumId, subType)
                .subscribe({
                    viewState.onAddToFavorite(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun addTopicToFavorite(topicId: Int, subType: String) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_ADD, -1, topicId, subType)
                .subscribe({
                    viewState.onAddToFavorite(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun markRead() {
        forumRepository
                .markRead(id)
                .subscribe({
                    viewState.onMarkRead()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun openForum() {
        router.navigateTo(Screen.Forum().apply {
            forumId = id
        })
    }

    fun openSearch() {
        router.navigateTo(Screen.Search().apply {
            searchUrl = "https://4pda.ru/forum/index.php?act=search&source=all&forums%5B%5D=$id"
        })
    }

    fun onItemClick(item: TopicItem) {
        if (item.isAnnounce) {
            val args = Bundle()
            args.putString(TabFragment.ARG_TITLE, item.title)
            IntentHandler.handle(item.announceUrl, args)
            return
        }
        if (item.isForum) {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.id)
            return
        }
        val args = Bundle()
        args.putString(TabFragment.ARG_TITLE, item.title)
        IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + item.id + "&view=getnewpost", args)
    }

    fun onItemLongClick(item: TopicItem) {
        viewState.showItemDialogMenu(item)
    }
}
