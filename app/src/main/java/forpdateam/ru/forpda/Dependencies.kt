package forpdateam.ru.forpda

import android.content.Context
import forpdateam.ru.forpda.client.Client
import forpdateam.ru.forpda.model.NetworkStateProvider
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.auth.AuthApi
import forpdateam.ru.forpda.model.data.remote.api.devdb.DevDbApi
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostApi
import forpdateam.ru.forpda.model.data.remote.api.events.NotificationEventsApi
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumApi
import forpdateam.ru.forpda.model.data.remote.api.mentions.MentionsApi
import forpdateam.ru.forpda.model.data.remote.api.news.NewsApi
import forpdateam.ru.forpda.model.data.remote.api.profile.ProfileApi
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsApi
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi
import forpdateam.ru.forpda.model.data.remote.api.search.SearchApi
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import forpdateam.ru.forpda.model.data.remote.api.topcis.TopicsApi
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.model.repository.history.HistoryRepository
import forpdateam.ru.forpda.model.repository.mentions.MentionsRepository
import forpdateam.ru.forpda.model.repository.news.NewsRepository
import forpdateam.ru.forpda.model.repository.posteditor.PostEditorRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
import forpdateam.ru.forpda.model.repository.search.SearchRepository
import forpdateam.ru.forpda.model.repository.theme.ThemeRepository
import forpdateam.ru.forpda.model.repository.topics.TopicsRepository
import forpdateam.ru.forpda.model.system.AppNetworkState
import forpdateam.ru.forpda.model.system.AppSchedulers

/**
 * Created by radiationx on 01.01.18.
 */

class Dependencies internal constructor(
        context: Context
) {
    var networkState: NetworkStateProvider = AppNetworkState(context)

    var schedulers: SchedulersProvider = AppSchedulers()

    private val webClient: IWebClient = Client(context)

    private val authApi = AuthApi(webClient)
    private val devDbApi = DevDbApi(webClient)
    private val editPostApi = EditPostApi(webClient)
    private val eventsApi = NotificationEventsApi(webClient)
    private val favoritesApi = FavoritesApi(webClient)
    private val forumApi = ForumApi(webClient)
    private val mentionsApi = MentionsApi(webClient)
    private val newsApi = NewsApi(webClient)
    private val profileApi = ProfileApi(webClient)
    private val qmsApi = QmsApi(webClient)
    private val reputationApi = ReputationApi(webClient)
    private val searchApi = SearchApi(webClient)
    private val themeApi = ThemeApi(webClient)
    private val topicsApi = TopicsApi(webClient)

    val favoritesRepository = FavoritesRepository(schedulers, favoritesApi)
    val historyRepository = HistoryRepository(schedulers)
    val mentionsRepository = MentionsRepository(schedulers, mentionsApi)
    val authRepository = AuthRepository(schedulers, authApi)
    val profileRepository = ProfileRepository(schedulers, profileApi)
    val reputationRepository = ReputationRepository(schedulers, reputationApi)
    val forumRepository = ForumRepository(schedulers, forumApi)
    val topicsRepository = TopicsRepository(schedulers, topicsApi)
    val themeRepository = ThemeRepository(schedulers, themeApi, editPostApi)
    val qmsRepository = QmsRepository(schedulers, qmsApi)
    val searchRepository = SearchRepository(schedulers, searchApi)
    val newsRepository = NewsRepository(schedulers, newsApi)
    val devDbRepository = DevDbRepository(schedulers, devDbApi)
    val editPostRepository = PostEditorRepository(schedulers, editPostApi)
}
