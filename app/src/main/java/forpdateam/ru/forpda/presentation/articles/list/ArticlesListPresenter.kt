package forpdateam.ru.forpda.presentation.articles.list

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.model.data.remote.api.news.Constants
import forpdateam.ru.forpda.model.repository.news.NewsRepository
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.news.details.NewsDetailsFragment
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticlesListPresenter(
        private val newsRepository: NewsRepository
) : BasePresenter<ArticlesListView>() {
    private val category = Constants.NEWS_CATEGORY_ROOT
    private var currentPage = 1

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refreshArticles()
    }

    private fun loadArticles(page: Int, withClear: Boolean) {
        currentPage = page
        newsRepository
                .getNews(category, currentPage)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showNews(it, withClear)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun refreshArticles() {
        loadArticles(1, true)
    }

    fun loadMore() {
        loadArticles(currentPage + 1, false)
    }

    fun onItemClick(item: NewsItem) {
        val args = Bundle()
        args.putInt(NewsDetailsFragment.ARG_NEWS_ID, item.id)
        args.putString(NewsDetailsFragment.ARG_NEWS_TITLE, item.title)
        args.putString(NewsDetailsFragment.ARG_NEWS_AUTHOR_NICK, item.author)
        args.putString(NewsDetailsFragment.ARG_NEWS_DATE, item.date)
        args.putString(NewsDetailsFragment.ARG_NEWS_IMAGE, item.imgUrl)
        args.putInt(NewsDetailsFragment.ARG_NEWS_COMMENTS_COUNT, item.commentsCount)
        args.putBoolean(NewsDetailsFragment.OTHER_CASE, true)
        TabManager.get().add(NewsDetailsFragment::class.java, args)
    }

    fun onItemLongClick(item: NewsItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: NewsItem) {
        Utils.copyToClipBoard("https://4pda.ru/index.php?p=${item.id}")
    }

    fun shareLink(item: NewsItem) {
        Utils.shareText("https://4pda.ru/index.php?p=${item.id}")
    }

    fun openProfile(item: NewsItem) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=${item.authorId}")
    }

    fun createNote(item: NewsItem) {
        val url = "https://4pda.ru/index.php?p=${item.id}"
        viewState.showCreateNote(item.title, url)
    }

    fun openSearch(){
        val url = "https://4pda.ru/?s="
        val args = Bundle()
        args.putString(TabFragment.ARG_TAB, url)
        TabManager.get().add(SearchFragment::class.java, args)
    }
}
