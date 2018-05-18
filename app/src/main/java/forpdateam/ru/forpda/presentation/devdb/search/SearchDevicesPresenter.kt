package forpdateam.ru.forpda.presentation.devdb.search

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.fragments.devdb.device.DeviceFragment
import forpdateam.ru.forpda.ui.fragments.devdb.search.DevDbSearchFragment

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class SearchDevicesPresenter(
        private val devDbRepository: DevDbRepository
) : BasePresenter<SearchDevicesView>() {

    var searchQuery: String? = null

    var currentData: Brand? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }

    fun refresh() = search(searchQuery)

    fun search(query: String?) {
        searchQuery = query
        if (searchQuery.isNullOrEmpty()) {
            return
        }
        devDbRepository
                .search(searchQuery.orEmpty())
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    viewState.showData(it, searchQuery.orEmpty())
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun openDevice(item: Brand.DeviceItem) {
        currentData?.let {
            val args = Bundle()
            args.putString(DeviceFragment.ARG_DEVICE_ID, item.id)
            TabManager.get().add(DeviceFragment::class.java, args)
        }
    }

    fun openSearch() {
        TabManager.get().add(DevDbSearchFragment::class.java)
    }

    fun copyLink(item: Brand.DeviceItem) {
        currentData?.let {
            Utils.copyToClipBoard("https://4pda.ru/devdb/${item.id}")
        }
    }

    fun shareLink(item: Brand.DeviceItem) {
        currentData?.let {
            Utils.shareText("https://4pda.ru/devdb/${item.id}")
        }
    }

    fun createNote(item: Brand.DeviceItem) {
        currentData?.let {
            val title = "DevDb: ${it.title} ${item.title}"
            val url = "https://4pda.ru/devdb/" + item.id
            viewState.showCreateNote(title, url)
        }
    }
}
