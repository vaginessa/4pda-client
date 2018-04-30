package forpdateam.ru.forpda.model.repository.events

import android.content.Context
import android.support.v4.util.ArraySet
import android.util.Log
import com.jakewharton.rxrelay2.PublishRelay
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.model.NetworkStateProvider
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.events.NotificationEventsApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeoutException

class EventsRepository(
        private val context: Context,
        private val webClient: IWebClient,
        private val eventsApi: NotificationEventsApi,
        private val schedulers: SchedulersProvider,
        private val networkStateProvider: NetworkStateProvider
) {
    companion object {
        private const val LOG_TAG = "EventsRepository"
        private const val STACKED_MAX = 4
    }

    private var timerPeriod = (10 * 1000).toLong()

    private val pendingEvents = mapOf<NotificationEvent.Source, MutableMap<Int, NotificationEvent>>(
            NotificationEvent.Source.QMS to mutableMapOf(),
            NotificationEvent.Source.THEME to mutableMapOf(),
            NotificationEvent.Source.SITE to mutableMapOf()
    )

    private var checkTimer: Timer? = null
    private val timerRunnable = {
        for (source in pendingEvents.keys) {
            handlePendingEvents(source)
        }
    }
    private var connected = false
    private val eventsHistory = mutableMapOf<Int, NotificationEvent>()
    private var webSocket: WebSocket? = null


    private val notifyRelay = PublishRelay.create<NotificationEvent>()
    private val notifyStackRelay = PublishRelay.create<List<NotificationEvent>>()
    private val cancelRelay = PublishRelay.create<NotificationEvent>()
    private val notifyTabRelay = PublishRelay.create<TabNotification>()

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(LOG_TAG, "WSListener onOpen: " + response.toString())
            connected = true
            webSocket.send("""[0, "sv"]""")
            webSocket.send("""[0, "ea", "u${ClientHelper.getUserId()}"]""")
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            Log.d(LOG_TAG, "WSListener onMessage: $text")
            try {
                eventsApi.parseWebSocketEvent(text)?.also {
                    if (it.type != NotificationEvent.Type.HAT_EDITED) {
                        handleWebSocketEvent(it)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            Log.d(LOG_TAG, "WSListener onClosed: $code $reason")
            stop()
        }

        override fun onFailure(webSocket: WebSocket?, throwable: Throwable, response: Response?) {
            Log.d(LOG_TAG, "WSListener onFailure: ${throwable.message} $response")
            if (response != null) {
                Log.d(LOG_TAG, "WSListener onFailure: code=${response.code()}")
                if (response.code() == 403) {
                    App.get().notifyForbidden(true)
                }
            }

            throwable.printStackTrace()
            stop()
            if (throwable is SocketTimeoutException || throwable is TimeoutException) {
                start(true)
            }
        }
    }

    init {
        networkStateProvider
                .observeState()
                .subscribe({
                    if (it) {
                        start(true)
                    }
                })

        timerPeriod = Preferences.Notifications.Main.getLimit(context)
    }

    fun observeEvents(): Observable<NotificationEvent> = notifyRelay
            .observeOn(schedulers.ui())

    fun observeEventsStack(): Observable<List<NotificationEvent>> = notifyStackRelay
            .observeOn(schedulers.ui())

    fun observeCancel(): Observable<NotificationEvent> = cancelRelay
            .observeOn(schedulers.ui())

    fun observeEventsTab(): Observable<TabNotification> = notifyTabRelay
            .observeOn(schedulers.ui())

    fun setTimerPeriod(period: Long) {
        timerPeriod = period
        resetTimer()
    }

    fun externalStart(checkEvents: Boolean) {
        start(checkEvents)
    }

    fun updateEvents(source: NotificationEvent.Source) {
        hardHandleEvent(source)
    }

    private fun start(checkEvents: Boolean) {
        Log.e(LOG_TAG, "Start: ${networkStateProvider.getState()} : $connected : $checkEvents : $webSocket : $webSocketListener")
        if (networkStateProvider.getState()) {
            if (!connected) {
                webSocket = webClient.createWebSocketConnection(webSocketListener)
                connected = true
            }

            if (checkEvents) {
                hardHandleEvent(NotificationEvent.Source.THEME)
                hardHandleEvent(NotificationEvent.Source.QMS)
            }
            Log.d("SUKA", "PERIOD BLYAD $timerPeriod")
            resetTimer()
        }
    }

    private fun stop() {
        cancelTimer()
        connected = false
        webSocket?.cancel()
        webSocket = null
    }

    private fun resetTimer() {
        cancelTimer()
        checkTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    timerRunnable.invoke()
                }
            }, 0, timerPeriod)
        }
    }

    private fun cancelTimer() {
        checkTimer?.apply {
            cancel()
            purge()
        }
        checkTimer = null
    }

    private fun sendNotification(event: NotificationEvent) {
        if (event.userId == ClientHelper.getUserId()) {
            return
        }
        if (!checkNotify(event, event.source)) {
            return
        }
        notifyRelay.accept(event)
    }

    private fun sendNotifications(events: List<NotificationEvent>, tSource: NotificationEvent.Source) {
        if (events.isEmpty()) {
            return
        }
        if (events.size <= STACKED_MAX) {
            for (event in events) {
                sendNotification(event)
            }
            return
        }
        if (!checkNotify(null, tSource)) {
            return
        }
        notifyStackRelay.accept(events)
    }

    private fun notifyTabs(event: TabNotification) {
        Log.d("SUKA", "notifyTabs")
        notifyTabRelay.accept(event)
    }

    private fun checkNotify(event: NotificationEvent?, source: NotificationEvent.Source): Boolean {
        if (!Preferences.Notifications.Main.isEnabled(context)) {
            return false
        }
        if (NotificationEvent.fromQms(source)) {
            if (!Preferences.Notifications.Qms.isEnabled(context)) {
                return false
            }
        } else if (NotificationEvent.fromTheme(source)) {
            if (event != null && event.isMention) {
                if (!Preferences.Notifications.Mentions.isEnabled(context)) {
                    return false
                }
            } else {
                if (!Preferences.Notifications.Favorites.isEnabled(context)) {
                    return false
                }
            }
        }
        return true
    }

    private fun checkOldEvent(event: NotificationEvent) {
        var oldEvent = eventsHistory[event.notifyId(NotificationEvent.Type.NEW)]
        var delete = false

        if (event.fromTheme()) {
            //Убираем уведомления избранного
            if (oldEvent != null && event.messageId >= oldEvent.messageId) {
                cancelRelay.accept(oldEvent)
                delete = true
            }

            //Убираем уведомление упоминаний
            oldEvent = eventsHistory[event.notifyId(NotificationEvent.Type.MENTION)]
            if (oldEvent != null) {
                cancelRelay.accept(oldEvent)
                delete = true
            }
        } else if (event.fromQms()) {

            //Убираем уведомление кумыса
            if (oldEvent != null) {
                cancelRelay.accept(oldEvent)
                delete = true
            }
        }

        if (delete) {
            eventsHistory.remove(event.notifyId(NotificationEvent.Type.NEW))
        }
    }

    private fun checkOldEvents(loadedEvents: List<NotificationEvent>, source: NotificationEvent.Source) {
        val oldEvents = eventsHistory.filter { it.value.source == source }.map { it.value }

        for (oldEvent in oldEvents) {
            var exist = false
            for (loadedEvent in loadedEvents) {
                if (oldEvent.sourceId == loadedEvent.sourceId) {
                    exist = true
                    break
                }
            }
            if (!exist) {
                cancelRelay.accept(oldEvent)
                eventsHistory.remove(oldEvent.notifyId(NotificationEvent.Type.NEW))
                val tabNotification = TabNotification()
                tabNotification.source = oldEvent.source
                tabNotification.event = oldEvent
                tabNotification.type = NotificationEvent.Type.READ
                tabNotification.isWebSocket = true
                notifyTabs(tabNotification)
            }
        }
    }

    private fun handleWebSocketEvent(event: NotificationEvent) {
        val tabNotification = TabNotification()
        tabNotification.type = event.type
        tabNotification.source = event.source
        tabNotification.event = event
        tabNotification.isWebSocket = true
        notifyTabs(tabNotification)

        if (event.isRead) {
            checkOldEvent(event)
            return
        }
        handleEvent(listOf(event), event.source)
    }


    private fun handleEvent(events: List<NotificationEvent>, source: NotificationEvent.Source) {
        val pending = pendingEvents[source]
        if (pending != null) {
            for (event in events) {
                pending[event.sourceId] = event
            }
        }
    }

    private fun hardHandleEvent(source: NotificationEvent.Source) {
        hardHandleEvent(emptyList(), source)
    }

    private fun hardHandleEvent(events: List<NotificationEvent>, source: NotificationEvent.Source) {
        Log.d("SUKA", "hardHandleEvent " + events.size + " : " + source)
        if (NotificationEvent.fromSite(source)) {
            if (Preferences.Notifications.Mentions.isEnabled(context)) {
                for (event in events) {
                    sendNotification(event)
                }
            }
            return
        }

        var observable: Observable<List<NotificationEvent>>? = null
        if (NotificationEvent.fromQms(source)) {
            observable = Observable.fromCallable { eventsApi.qmsEvents }
        } else if (NotificationEvent.fromTheme(source)) {
            observable = Observable.fromCallable { eventsApi.favoritesEvents }
        }

        if (observable != null) {
            observable
                    .onErrorReturnItem(emptyList())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ loadedEvents ->
                        val savedEvents = getSavedEvents(source)
                        //savedEvents = mutableListOf();
                        saveEvents(loadedEvents, source)
                        val newEvents = compareEvents(savedEvents, loadedEvents, events, source)
                        val stackedNewEvents = newEvents.toMutableList()

                        checkOldEvents(loadedEvents, source)

                        //Удаляем из общего уведомления текущие уведомление
                        for (event in events) {
                            for (newEvent in newEvents) {
                                if (newEvent.sourceId == event.sourceId) {
                                    stackedNewEvents.remove(newEvent)
                                    newEvent.type = event.type
                                    newEvent.messageId = event.messageId

                                    val tabNotification = TabNotification()
                                    tabNotification.type = newEvent.type
                                    tabNotification.source = newEvent.source
                                    tabNotification.event = newEvent
                                    tabNotification.loadedEvents.addAll(loadedEvents)
                                    tabNotification.newEvents.addAll(newEvents)
                                    tabNotification.isWebSocket = false
                                    notifyTabs(tabNotification)

                                    sendNotification(event)
                                } else if (event.isMention && !Preferences.Notifications.Favorites.isEnabled(context)) {
                                    stackedNewEvents.remove(newEvent)
                                }
                            }
                        }

                        sendNotifications(stackedNewEvents, source)
                    }, {
                        it.printStackTrace()
                    })
        }
    }


    private fun handlePendingEvents(source: NotificationEvent.Source) {
        val pending = pendingEvents[source]
        if (pending != null && pending.isNotEmpty()) {
            hardHandleEvent(pending.map { it.value }, source)
            pending.clear()
        }
    }


    private fun getSavedEvents(source: NotificationEvent.Source): List<NotificationEvent> {
        var prefKey = ""
        if (NotificationEvent.fromQms(source)) {
            prefKey = Preferences.Notifications.Data.QMS_EVENTS
        } else if (NotificationEvent.fromTheme(source)) {
            prefKey = Preferences.Notifications.Data.FAVORITES_EVENTS
        }

        val savedEvents = App.get().preferences.getStringSet(prefKey, ArraySet())
        val responseBuilder = StringBuilder()
        for (saved in savedEvents) {
            responseBuilder.append(saved).append('\n')
        }
        val response = responseBuilder.toString()

        if (NotificationEvent.fromQms(source)) {
            return eventsApi.getQmsEvents(response)
        } else if (NotificationEvent.fromTheme(source)) {
            return eventsApi.getFavoritesEvents(response)
        }
        return emptyList()
    }

    private fun saveEvents(loadedEvents: List<NotificationEvent>, source: NotificationEvent.Source) {
        var prefKey = ""
        if (NotificationEvent.fromQms(source)) {
            prefKey = Preferences.Notifications.Data.QMS_EVENTS
        } else if (NotificationEvent.fromTheme(source)) {
            prefKey = Preferences.Notifications.Data.FAVORITES_EVENTS
        }

        val savedEvents = ArraySet<String>()
        for (event in loadedEvents) {
            savedEvents.add(event.sourceEventText)
        }
        App.get().preferences.edit().putStringSet(prefKey, savedEvents).apply()
    }

    private fun compareEvents(
            savedEvents: List<NotificationEvent>,
            loadedEvents: List<NotificationEvent>,
            events: List<NotificationEvent>,
            source: NotificationEvent.Source
    ): List<NotificationEvent> {
        val newEvents = mutableListOf<NotificationEvent>()

        for (loaded in loadedEvents) {
            var isNew = true
            for (saved in savedEvents) {
                if (loaded.sourceId == saved.sourceId && loaded.timeStamp <= saved.timeStamp) {
                    isNew = false
                    break
                }
            }

            if (isNew) {
                newEvents.add(loaded)
            }
        }

        if (NotificationEvent.fromTheme(source) && Preferences.Notifications.Favorites.isOnlyImportant(context)) {
            val toRemove = mutableListOf<NotificationEvent>()
            for (newEvent in newEvents) {
                var remove = false
                for (event in events) {
                    if (!event.isMention && !newEvent.isImportant) {
                        remove = true
                        break
                    }
                }
                if (!newEvent.isImportant) {
                    remove = true
                }
                if (remove) {
                    toRemove.add(newEvent)
                }
            }
            for (removeEvent in toRemove) {
                newEvents.remove(removeEvent)
            }
            toRemove.clear()
        }

        return newEvents
    }


}