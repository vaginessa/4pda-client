package forpdateam.ru.forpda.presentation.qms.blacklist

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.fragments.qms.QmsBlackListFragment
import forpdateam.ru.forpda.ui.fragments.qms.QmsThemesFragment

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsBlackListPresenter(
        private val qmsRepository: QmsRepository
) : BasePresenter<QmsBlackListView>() {


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadContacts()
    }


    fun loadContacts() {
        qmsRepository
                .getContactList()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showContacts(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun blockUser(nick: String) {
        qmsRepository
                .blockUser(nick)
                .subscribe({
                    viewState.showContacts(it)
                    viewState.clearNickField()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun unBlockUser(id: Int) {
        qmsRepository
                .unBlockUsers(id)
                .subscribe({
                    viewState.showContacts(it)
                    viewState.clearNickField()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun searchUser(nick: String) {
        qmsRepository
                .findUser(nick)
                .subscribe({
                    viewState.showFoundUsers(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun onItemLongClick(item: QmsContact) {
        viewState.showItemDialogMenu(item)
    }

    fun openProfile(item: QmsContact) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=${item.id}")
    }

    fun openDialogs(item: QmsContact) {
        val args = Bundle()
        args.putString(TabFragment.ARG_TITLE, item.nick)
        args.putInt(QmsThemesFragment.USER_ID_ARG, item.id)
        args.putString(QmsThemesFragment.USER_AVATAR_ARG, item.avatar)
        TabManager.get().add(QmsThemesFragment::class.java, args)
    }
}
