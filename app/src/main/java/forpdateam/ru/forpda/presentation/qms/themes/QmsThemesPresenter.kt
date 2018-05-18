package forpdateam.ru.forpda.presentation.qms.themes

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.IRouter
import forpdateam.ru.forpda.presentation.Screen

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsThemesPresenter(
        private val qmsRepository: QmsRepository,
        private val router: IRouter,
        private val linkHandler: ILinkHandler
) : BasePresenter<QmsThemesView>() {

    var themesId: Int = 0
    var avatarUrl: String? = null
    var currentData: QmsThemes? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        avatarUrl?.let { viewState.showAvatar(it) }
        loadCache()
        loadThemes()
    }

    fun loadCache() {
        qmsRepository
                .getThemesCache(themesId)
                .subscribe({
                    currentData = it
                    viewState.showThemes(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun loadThemes() {
        qmsRepository
                .getThemesList(themesId)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    viewState.showThemes(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun blockUser() {
        currentData?.nick?.let { nick ->
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
    }

    fun deleteTheme(themeId: Int) {
        currentData?.let {
            qmsRepository
                    .deleteTheme(it.userId, themeId)
                    .subscribe({
                        viewState.showThemes(it)
                    }, {
                        it.printStackTrace()
                    })
                    .addToDisposable()
        }
    }

    fun openProfile(userId: Int) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showuser=$userId", router)
    }

    fun openChat() {
        currentData?.let {
            router.navigateTo(Screen.QmsChat().apply {
                userId = it.userId
                userNick = it.nick
                avatarUrl = this@QmsThemesPresenter.avatarUrl
            })
        }
    }

    fun createNote() {
        currentData?.let {
            val url = "https://4pda.ru/forum/index.php?act=qms&mid=${it.userId}"
            viewState.showCreateNote(it.nick, url)
        }
    }

    fun createThemeNote(item: QmsTheme) {
        currentData?.let {
            val url = "https://4pda.ru/forum/index.php?act=qms&mid=${it.userId}&t=${item.userId}"
            viewState.showCreateNote(item.name, it.nick, url)
        }
    }

    fun onItemClick(item: QmsTheme) {
        currentData?.let {
            router.navigateTo(Screen.QmsChat().apply {
                screenTitle = item.name
                screenSubTitle = it.nick
                userId = it.userId
                avatarUrl = this@QmsThemesPresenter.avatarUrl
                themeId = item.id
                themeTitle = item.name
            })
        }
    }

    fun onItemLongClick(item: QmsTheme) {
        viewState.showItemDialogMenu(item)
    }

}
