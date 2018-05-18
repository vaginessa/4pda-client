package forpdateam.ru.forpda.presentation.qms.blacklist

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.presentation.IRouter
import forpdateam.ru.forpda.presentation.Screen

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsBlackListPresenter(
        private val qmsRepository: QmsRepository,
        private val router: IRouter
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
        router.navigateTo(Screen.QmsThemes().apply {
            screenTitle = item.nick
            userId = item.id
            avatarUrl = item.avatar
        })
    }
}
