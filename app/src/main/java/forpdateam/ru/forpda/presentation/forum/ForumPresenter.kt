package forpdateam.ru.forpda.presentation.forum

import android.os.Bundle

import com.arellomobile.mvp.InjectViewState

import forpdateam.ru.forpda.api.forum.models.ForumItemTree
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment
import io.reactivex.disposables.Disposable

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
class ForumPresenter(
        private val forumRepository: ForumRepository
) : BasePresenter<ForumView>() {

    fun loadForums() {
        val disposable = forumRepository.getForums()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showForums(it)
                    saveCacheForums(it)
                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
    }

    fun getCacheForums() {
        val disposable = forumRepository.getCache()
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
        addToDisposable(disposable)
    }

    private fun saveCacheForums(rootForum: ForumItemTree) {
        val disposable = forumRepository.saveCache(rootForum)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({

                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
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
