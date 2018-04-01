package forpdateam.ru.forpda.presentation.qms.themes

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.qms.chat.QmsChatFragment

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsThemesPresenter(
        private val qmsRepository: QmsRepository
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
        IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=$userId")
    }

    fun openChat() {
        currentData?.let {
            val args = Bundle()
            args.putInt(QmsChatFragment.USER_ID_ARG, it.userId)
            args.putString(QmsChatFragment.USER_NICK_ARG, it.nick)
            args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl)
            TabManager.get().add(QmsChatFragment::class.java, args)
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
            val args = Bundle()
            args.putString(TabFragment.ARG_TITLE, item.name)
            args.putString(TabFragment.TAB_SUBTITLE, it.nick)
            args.putInt(QmsChatFragment.USER_ID_ARG, it.userId)
            args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl)
            args.putInt(QmsChatFragment.THEME_ID_ARG, item.id)
            args.putString(QmsChatFragment.THEME_TITLE_ARG, item.name)
            TabManager.get().add(QmsChatFragment::class.java, args)
        }
    }

    fun onItemLongClick(item: QmsTheme) {
        viewState.showItemDialogMenu(item)
    }

}
