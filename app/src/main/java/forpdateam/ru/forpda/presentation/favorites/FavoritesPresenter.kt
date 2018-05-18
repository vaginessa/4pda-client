package forpdateam.ru.forpda.presentation.favorites

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import forpdateam.ru.forpda.model.repository.events.EventsRepository
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.IRouter
import forpdateam.ru.forpda.presentation.Screen

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class FavoritesPresenter(
        private val favoritesRepository: FavoritesRepository,
        private val forumRepository: ForumRepository,
        private val eventsRepository: EventsRepository,
        private val router: IRouter,
        private val linkHandler: ILinkHandler
) : BasePresenter<FavoritesView>() {


    var loadAll = false
    var currentSt = 0
    var sorting: Sorting? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        eventsRepository
                .observeEventsTab()
                .subscribe {
                    handleEvent(it)
                }
                .addToDisposable()
        loadFavorites()
    }

    fun updateSorting(key: String, order: String) {
        sorting?.also {
            it.key = key
            it.order = order
        }
    }

    fun loadFavorites() {
        favoritesRepository
                .loadFavorites(currentSt, loadAll, sorting)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.onLoadFavorites(it)
                    showFavorites()
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun showFavorites() {
        favoritesRepository
                .cache
                .subscribe {
                    viewState.onShowFavorite(it)
                }
                .addToDisposable()
    }

    fun markRead(topicId: Int) {
        favoritesRepository
                .markRead(topicId)
                .subscribe({
                    this.showFavorites()
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun handleEvent(event: TabNotification) {
        if (!Preferences.Notifications.Favorites.isLiveTab(App.getContext())) return
        if (event.isWebSocket && event.event.isNew) return
        favoritesRepository
                .handleEvent(event, sorting, ClientHelper.getFavoritesCount())
                .subscribe({
                    viewState.onHandleEvent(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun markAllRead() {
        forumRepository
                .markAllRead()
                .subscribe({
                    viewState.onMarkAllRead()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun onItemClick(item: FavItem) {
        val args = mapOf<String, String>(
                Screen.ARG_TITLE to item.topicTitle
        )
        if (item.isForum) {
            linkHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.forumId, router, args)
        } else {
            linkHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + item.topicId + "&view=getnewpost", router, args)
        }
    }

    fun onItemLongClick(item: FavItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: FavItem) {
        if (item.isForum) {
            Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showforum=" + Integer.toString(item.forumId))
        } else {
            Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showtopic=" + Integer.toString(item.topicId))
        }
    }

    fun openAttachments(item: FavItem) {
        linkHandler.handle("https://4pda.ru/forum/index.php?act=attach&code=showtopic&tid=" + item.topicId, router)
    }

    fun openForum(item: FavItem) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.forumId, router)
    }

    fun changeFav(action: Int, type: String?, favId: Int) {
        favoritesRepository
                .editFavorites(action, favId, favId, type)
                .subscribe({
                    viewState.onChangeFav(it)
                    loadFavorites()
                }, {
                    it.printStackTrace()
                })
    }

    fun showSubscribeDialog(item: FavItem) {
        viewState.showSubscribeDialog(item)
    }
}
