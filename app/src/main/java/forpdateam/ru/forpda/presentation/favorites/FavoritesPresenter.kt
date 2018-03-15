package forpdateam.ru.forpda.presentation.favorites

import android.os.Bundle

import com.arellomobile.mvp.InjectViewState

import forpdateam.ru.forpda.api.favorites.Sorting
import forpdateam.ru.forpda.api.favorites.models.FavItem
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.ui.fragments.TabFragment
import io.reactivex.disposables.Disposable

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class FavoritesPresenter(
        private val favoritesRepository: FavoritesRepository
) : BasePresenter<FavoritesView>() {

    fun getFavorites(st: Int, all: Boolean, sorting: Sorting) {
        val disposable = favoritesRepository.loadFavorites(st, all, sorting)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.onLoadFavorites(it)
                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
    }

    fun saveFavorites(items: List<FavItem>) {
        val disposable = favoritesRepository.saveFavorites(items)
                .subscribe({
                    this.showFavorites()
                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
    }

    fun showFavorites() {
        val disposable = favoritesRepository.cache
                .subscribe {
                    viewState.onShowFavorite(it)
                }
        addToDisposable(disposable)
    }

    fun markRead(topicId: Int) {
        val disposable = favoritesRepository.markRead(topicId)
                .subscribe({
                    this.showFavorites()
                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
    }

    fun handleEvent(event: TabNotification, sorting: Sorting, count: Int) {
        if (event.isWebSocket && event.event.isNew) return
        val disposable = favoritesRepository.handleEvent(event, sorting, count)
                .subscribe({
                    viewState.onHandleEvent(it)
                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
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

    fun changeFav(action: Int, type: String, favId: Int) {
        viewState.changeFav(action, type, favId)
    }

    fun showSubscribeDialog(item: FavItem) {
        viewState.showSubscribeDialog(item)
    }
}
