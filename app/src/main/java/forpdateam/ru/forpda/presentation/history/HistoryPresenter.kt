package forpdateam.ru.forpda.presentation.history

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.model.repository.history.HistoryRepository
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class HistoryPresenter(
        private val historyRepository: HistoryRepository,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler
) : BasePresenter<HistoryView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getHistory()
    }

    fun getHistory() {
        historyRepository
                .getHistory()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showHistory(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun remove(id: Int) {
        historyRepository
                .remove(id)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    this.getHistory()
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun clear() {
        historyRepository
                .clear()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    this.getHistory()
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun copyLink(item: HistoryItem) {
        Utils.copyToClipBoard(item.url)
    }

    fun onItemClick(item: HistoryItem) {
        linkHandler.handle(item.url, router, mapOf(
                Screen.ARG_TITLE to  item.title
        ))
    }

    fun onItemLongClick(item: HistoryItem) {
        viewState.showItemDialogMenu(item)
    }
}
