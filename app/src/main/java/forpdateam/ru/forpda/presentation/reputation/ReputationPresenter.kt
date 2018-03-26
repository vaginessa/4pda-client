package forpdateam.ru.forpda.presentation.reputation

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.entity.remote.reputation.RepItem
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
class ReputationPresenter(
        private val reputationRepository: ReputationRepository
) : BasePresenter<ReputationView>() {

    var currentData = RepData()

    fun loadReputation() {
        reputationRepository
                .loadReputation(currentData)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showReputation(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun changeReputation(type: Boolean, message: String) {
        reputationRepository
                .changeReputation(0, currentData.id, type, message)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.onChangeReputation(it)
                    loadReputation()
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun selectPage(page: Int) {
        currentData.pagination.st = page
        loadReputation()
    }

    fun setSort(sort: String) {
        currentData.sort = sort
        loadReputation()
    }

    fun changeReputationMode() {
        currentData.mode = if (currentData.mode == ReputationApi.MODE_FROM) ReputationApi.MODE_TO else ReputationApi.MODE_FROM
        loadReputation()
    }

    fun onItemClick(item: RepItem) {
        viewState.showItemDialogMenu(item)
    }

    fun onItemLongClick(item: RepItem) {
        viewState.showItemDialogMenu(item)
    }

    fun navigateToProfile(userId: Int) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=$userId")
    }

    fun navigateToMessage(item: RepItem) {
        IntentHandler.handle(item.sourceUrl)
    }
}
