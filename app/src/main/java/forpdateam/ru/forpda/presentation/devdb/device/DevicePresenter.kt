package forpdateam.ru.forpda.presentation.devdb.device

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.fragments.devdb.search.SearchFragment

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class DevicePresenter(
        private val devDbRepository: DevDbRepository
) : BasePresenter<DeviceView>() {

    var deviceId: String? = null
    var currentData: Device? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadBrand()
    }

    fun loadBrand() {
        devDbRepository
                .getDevice(deviceId.orEmpty())
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    viewState.showData(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }


    fun openSearch() {
        TabManager.get().add(SearchFragment::class.java)
    }

    fun copyLink() {
        currentData?.let {
            Utils.copyToClipBoard("https://4pda.ru/index.php?p=${it.id}")
        }
    }

    fun shareLink() {
        currentData?.let {
            Utils.shareText("https://4pda.ru/devdb/${it.id}")
        }
    }

    fun createNote() {
        currentData?.let {
            val title = "DevDb: ${it.brandTitle} ${it.title}"
            val url = "https://4pda.ru/devdb/${it.id}"
            viewState.showCreateNote(title, url)
        }
    }

    fun openDevices() {
        currentData?.let {
            IntentHandler.handle("https://4pda.ru/devdb/${it.catId}/${it.brandId}")
        }
    }

    fun openBrands() {
        currentData?.let {
            IntentHandler.handle("https://4pda.ru/devdb/${it.catId}")
        }
    }
}
