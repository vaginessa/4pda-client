package forpdateam.ru.forpda.presentation.qms.contacts

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.qms.QmsBlackListFragment
import forpdateam.ru.forpda.ui.fragments.qms.QmsThemesFragment

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsContactsPresenter(
        private val qmsRepository: QmsRepository
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
        val args = Bundle()
        args.putString(TabFragment.ARG_TITLE, item.nick)
        args.putInt(QmsThemesFragment.USER_ID_ARG, item.id)
        args.putString(QmsThemesFragment.USER_AVATAR_ARG, item.avatar)
        TabManager.get().add(QmsThemesFragment::class.java, args)
    }

    fun onItemLongClick(item: QmsContact) {
        viewState.showItemDialogMenu(item)
    }

    fun createNote(item: QmsContact) {
        val url = "https://4pda.ru/forum/index.php?act=qms&mid=${item.id}"
        viewState.showCreateNote(item.nick, url)
    }

    fun openProfile(item: QmsContact) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=${item.id}")
    }

    fun openBlackList(){
        TabManager.get().add(QmsBlackListFragment::class.java)
    }
}
