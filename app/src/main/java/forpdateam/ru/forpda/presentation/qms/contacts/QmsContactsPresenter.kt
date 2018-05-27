package forpdateam.ru.forpda.presentation.qms.contacts

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsContactsPresenter(
        private val qmsRepository: QmsRepository,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val countersHolder: CountersHolder
) : BasePresenter<QmsContactsView>() {

    private val localItems = mutableListOf<QmsContact>()
    private val searchContacts = mutableListOf<QmsContact>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadCache()
        loadContacts()
    }

    fun searchLocal(nick: String) {
        searchContacts.clear()
        if (!nick.isEmpty()) {
            searchContacts.filter { it.nick.toLowerCase().contains(nick.toLowerCase()) }
            viewState.showContacts(searchContacts)
        } else {
            viewState.showContacts(localItems)
        }
    }

    fun loadCache() {
        qmsRepository
                .getContactsCache()
                .subscribe({
                    localItems.clear()
                    localItems.addAll(it)
                    viewState.showContacts(it)
                    countersHolder.set(countersHolder.get().apply {
                        qms = it.sumBy { it.count }
                    })
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun loadContacts() {
        qmsRepository
                .getContactList()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    localItems.clear()
                    localItems.addAll(it)
                    viewState.showContacts(it)
                    countersHolder.set(countersHolder.get().apply {
                        qms = it.sumBy { it.count }
                    })
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun deleteDialog(id: Int) {
        qmsRepository
                .deleteDialog(id)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    loadContacts()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun blockUser(nick: String) {
        qmsRepository
                .blockUser(nick)
                .map { it.firstOrNull { it.nick == nick } != null }
                .subscribe({
                    viewState.onBlockUser(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun onItemClick(item: QmsContact) {
        router.navigateTo(Screen.QmsThemes().apply {
            screenTitle = item.nick
            userId = item.id
            avatarUrl = item.avatar
        })
    }

    fun onItemLongClick(item: QmsContact) {
        viewState.showItemDialogMenu(item)
    }

    fun createNote(item: QmsContact) {
        val url = "https://4pda.ru/forum/index.php?act=qms&mid=${item.id}"
        viewState.showCreateNote(item.nick, url)
    }

    fun openProfile(item: QmsContact) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showuser=${item.id}", router)
    }

    fun openBlackList() {
        router.navigateTo(Screen.QmsBlackList())
    }

    fun openChatCreator(){
        router.navigateTo(Screen.QmsChat())
    }
}
