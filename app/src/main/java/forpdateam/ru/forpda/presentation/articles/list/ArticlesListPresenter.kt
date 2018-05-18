package forpdateam.ru.forpda.presentation.articles.list

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.model.data.remote.api.news.Constants
import forpdateam.ru.forpda.model.repository.news.NewsRepository
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.IRouter
import forpdateam.ru.forpda.presentation.Screen

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticlesListPresenter(
        private val newsRepository: NewsRepository,
        private val router: IRouter,
        private val linkHandler: ILinkHandler
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
        router.navigateTo(Screen.ArticleDetail().apply {
            articleId = item.id
            articleTitle = item.title
            articleAuthorNick = item.author
            articleDate = item.date
            articleImageUrl = item.imgUrl
            articleCommentsCount = item.commentsCount
        })
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
        linkHandler.handle("https://4pda.ru/forum/index.php?showuser=${item.authorId}", router)
    }

    fun createNote(item: NewsItem) {
        val url = "https://4pda.ru/index.php?p=${item.id}"
        viewState.showCreateNote(item.title, url)
    }

    fun openSearch() {
        router.navigateTo(Screen.Search().apply {
            searchUrl = "https://4pda.ru/?s="
        })
    }
}
