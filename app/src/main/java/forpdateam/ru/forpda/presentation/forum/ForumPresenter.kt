package forpdateam.ru.forpda.presentation.forum

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
class ForumPresenter(
        private val forumRepository: ForumRepository,
        private val favoritesRepository: FavoritesRepository
) : BasePresenter<ForumView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getCacheForums()
        loadForums()
    }

    fun loadForums() {
        forumRepository
                .getForums()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showForums(it)
                    saveCacheForums(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun getCacheForums() {
        forumRepository
                .getCache()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ it ->
                    if (it.forums == null) {
                        loadForums()
                    } else {
                        viewState.showForums(it)
                    }
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    private fun saveCacheForums(rootForum: ForumItemTree) {
        forumRepository
                .saveCache(rootForum)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({

                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun markRead(id: Int) {
        forumRepository
                .markRead(id)
                .subscribe({
                    viewState.onMarkRead()
                }, {
                    it.printStackTrace()
                })
    }

    fun markAllRead() {
        forumRepository
                .markAllRead()
                .subscribe({
                    viewState.onMarkAllRead()
                }, {
                    it.printStackTrace()
                })
    }

    fun addToFavorite(forumId: Int, subType: String) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_ADD_FORUM, -1, forumId, subType)
                .subscribe({
                    viewState.onAddToFavorite(it)
                }, {
                    it.printStackTrace()
                })
    }

    fun copyLink(item: ForumItemTree) {
        Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showforum=" + item.id)
    }

    fun navigateToForum(item: ForumItemTree) {
        val args = Bundle()
        args.putInt(TopicsFragment.TOPICS_ID_ARG, item.id)
        TabManager.get().add(TopicsFragment::class.java, args)
    }

    fun navigateToSearch(item: ForumItemTree) {
        val url = "https://4pda.ru/forum/index.php?act=search&source=all&forums%5B%5D=" + item.id
        val args = Bundle()
        args.putString(TabFragment.ARG_TAB, url)
        TabManager.get().add(SearchFragment::class.java, args)
    }
}
