package forpdateam.ru.forpda.presentation.favorites

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.ui.fragments.TabFragment

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class FavoritesPresenter(
        private val favoritesRepository: FavoritesRepository,
        private val forumRepository: ForumRepository
) : BasePresenter<FavoritesView>() {


    var loadAll = false
    var currentSt = 0
    var sorting: Sorting? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
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
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun saveFavorites(items: List<FavItem>) {
        favoritesRepository
                .saveFavorites(items)
                .subscribe({
                    this.showFavorites()
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

    fun handleEvent(event: TabNotification, count: Int) {
        if (event.isWebSocket && event.event.isNew) return
        favoritesRepository
                .handleEvent(event, sorting, count)
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
        val args = Bundle()
        args.putString(TabFragment.ARG_TITLE, item.topicTitle)
        if (item.isForum) {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.forumId, args)
        } else {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + item.topicId + "&view=getnewpost", args)
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
        IntentHandler.handle("https://4pda.ru/forum/index.php?act=attach&code=showtopic&tid=" + item.topicId)
    }

    fun openForum(item: FavItem) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.forumId)
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
