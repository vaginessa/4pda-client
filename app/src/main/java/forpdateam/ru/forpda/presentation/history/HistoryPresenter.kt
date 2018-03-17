package forpdateam.ru.forpda.presentation.history

import android.os.Bundle

import com.arellomobile.mvp.InjectViewState

import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.model.repository.history.HistoryRepository
import forpdateam.ru.forpda.ui.fragments.TabFragment
import io.reactivex.disposables.Disposable

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class HistoryPresenter(
        private val historyRepository: HistoryRepository
) : BasePresenter<HistoryView>() {

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
        val args = Bundle()
        args.putString(TabFragment.ARG_TITLE, item.title)
        IntentHandler.handle(item.url, args)
    }

    fun onItemLongClick(item: HistoryItem) {
        viewState.showItemDialogMenu(item)
    }
}
