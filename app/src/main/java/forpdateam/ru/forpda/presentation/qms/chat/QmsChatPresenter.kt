package forpdateam.ru.forpda.presentation.qms.chat

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.events.EventsRepository
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.presentation.IRouter
import forpdateam.ru.forpda.presentation.Screen

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsChatPresenter(
        private val qmsRepository: QmsRepository,
        private val qmsChatTemplate: QmsChatTemplate,
        private val avatarRepository: AvatarRepository,
        private val eventsRepository: EventsRepository,
        private val router: IRouter
) : BasePresenter<QmsChatView>(), IQmsChatPresenter {

    var themeId = 0
    var userId = 0
    var title: String? = null
    var nick: String? = null
    var avatarUrl: String? = null

    var currentData: QmsChatModel? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        eventsRepository
                .observeEventsTab()
                .subscribe {
                    handleEvent(it)
                }
                .addToDisposable()
        nick?.let { nick -> title?.let { title -> viewState.setTitles(title, nick) } }
        tryShowAvatar()
        loadChat()
    }

    fun findUser(nick: String) {
        qmsRepository
                .findUser(nick)
                .subscribe({
                    viewState.onShowSearchRes(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun loadChat() {
        qmsRepository
                .getChat(userId, themeId)
                //.map { qmsChatTemplate.mapEntity(it) }
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    viewState.showChat(it)
                    initOnNewMessages(it)
                    tryShowAvatar()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun sendNewTheme(nick: String, title: String, message: String) {
        qmsRepository
                .sendNewTheme(nick, title, message)
                //.map { qmsChatTemplate.mapEntity(it) }
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    viewState.onNewThemeCreate(it)
                    viewState.showChat(it)
                    initOnNewMessages(it)
                    tryShowAvatar()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun sendMessage(message: String) {
        qmsRepository
                .sendMessage(userId, themeId, message)
                .doOnTerminate { viewState.setMessageRefreshing(true) }
                .doAfterTerminate { viewState.setMessageRefreshing(false) }
                .subscribe({
                    viewState.onSentMessage(it)
                }, {
                    it.printStackTrace()
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

    private fun tryShowAvatar() {
        val result = avatarUrl?.let { it } ?: currentData?.avatarUrl?.let { it }
        if (result != null) {
            viewState.showAvatar(result)
        } else {
            currentData?.let {
                avatarRepository
                        .getAvatar(it.nick)
                        .subscribe({
                            viewState.showAvatar(it)
                        }, {
                            it.printStackTrace()
                        })
                        .addToDisposable()
            }
        }
    }


    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>) {
        qmsRepository
                .uploadFiles(files, pending)
                .subscribe({
                    viewState.onUploadFiles(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun handleEvent(event: TabNotification) {
        val themeId = event.event.sourceId
        val messageId = event.event.messageId
        currentData?.let {
            if (themeId == it.themeId) {
                when (event.type) {
                    NotificationEvent.Type.NEW -> {
                        onNewWsMessage(themeId, messageId)
                    }
                    NotificationEvent.Type.READ -> {
                        viewState.makeAllRead()
                    }
                    NotificationEvent.Type.MENTION -> {
                    }
                    NotificationEvent.Type.HAT_EDITED -> {
                    }
                    null -> {
                    }
                }
            }
        }

    }

    private fun onNewWsMessage(themeId: Int, messageId: Int) {
        currentData?.let {
            val lastMessId = it.messages.lastOrNull()?.id ?: 0
            qmsRepository
                    .getMessagesFromWs(themeId, messageId, lastMessId)
                    .subscribe({
                        onNewMessages(it)
                    }, {
                        it.printStackTrace()
                    })
                    .addToDisposable()
        }
    }

    fun checkNewMessages() {
        currentData?.let {
            val lastMessId = it.messages.lastOrNull()?.id ?: 0
            qmsRepository
                    .getMessagesAfter(themeId, it.themeId, lastMessId)
                    .subscribe({
                        onNewMessages(it)
                    }, {
                        it.printStackTrace()
                    })
                    .addToDisposable()
        }
    }

    private fun initOnNewMessages(data: QmsChatModel) {
        val end = data.messages.size
        val start = Math.max(end - 30, 0)
        data.showedMessIndex = start
        val newMessages = data.messages.subList(start, end)
        viewState.onNewMessages(newMessages)
    }

    private fun onNewMessages(items: List<QmsMessage>) {
        currentData?.let { data ->
            val result = items.filter { new ->
                data.messages.find { it.id != new.id } != null
            }
            data.messages.addAll(result)
            viewState.onNewMessages(result)
        }
    }

    fun createThemeNote() {
        currentData?.let {
            val url = "https://4pda.ru/forum/index.php?act=qms&mid=${it.userId}&t=${it.themeId}"
            viewState.showCreateNote(it.title, it.nick, url)
        }
    }

    fun openProfile() {
        currentData?.let {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=${it.userId}")
        }
    }

    fun openDialogs() {
        currentData?.let {
            router.navigateTo(Screen.QmsThemes().apply {
                screenTitle = it.nick
                userId = it.userId
                avatarUrl = it.avatarUrl
            })
        }
    }

    fun onSendClick() {
        if (themeId == QmsChatModel.NOT_CREATED) {
            viewState.temp_sendNewTheme()
        } else {
            viewState.temp_sendMessage()
        }
    }

    override fun loadMoreMessages() {
        currentData?.let {
            val endIndex = it.showedMessIndex
            val startIndex = Math.max(endIndex - 30, 0)
            it.showedMessIndex = startIndex
            viewState.showMoreMessages(it.messages, startIndex, endIndex)
        }
    }
}
