package forpdateam.ru.forpda

import android.content.Context
import forpdateam.ru.forpda.client.Client
import forpdateam.ru.forpda.model.NetworkStateProvider
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache
import forpdateam.ru.forpda.model.data.cache.forum.ForumCache
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import forpdateam.ru.forpda.model.data.cache.qms.QmsCache
import forpdateam.ru.forpda.model.data.providers.UserSourceProvider
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
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
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
import forpdateam.ru.forpda.model.system.AppTheme
import forpdateam.ru.forpda.presentation.announce.AnnounceTemplate
import forpdateam.ru.forpda.presentation.articles.detail.ArticleTemplate
import forpdateam.ru.forpda.presentation.forumrules.ForumRulesTemplate
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatTemplate
import forpdateam.ru.forpda.presentation.search.SearchTemplate
import forpdateam.ru.forpda.presentation.theme.ThemeTemplate
import forpdateam.ru.forpda.ui.AppThemeHolder
import forpdateam.ru.forpda.ui.TemplateManager

/**
 * Created by radiationx on 01.01.18.
 */

class Dependencies internal constructor(
        context: Context
) {
    val networkState: NetworkStateProvider = AppNetworkState(context)

    val schedulers: SchedulersProvider = AppSchedulers()

    val webClient: IWebClient = Client(context)

    val appTheme: AppThemeHolder = AppTheme(context)
    val templateManager = TemplateManager(context, appTheme)
    val themeTemplate = ThemeTemplate(templateManager)
    val articleTemplate = ArticleTemplate(templateManager)
    val searchTemplate = SearchTemplate(templateManager)
    val forumRulesTemplate = ForumRulesTemplate(templateManager)
    val announceTemplate = AnnounceTemplate(templateManager)
    val qmsChatTemplate = QmsChatTemplate(templateManager)

    val authApi = AuthApi(webClient)
    val devDbApi = DevDbApi(webClient)
    val themeApi = ThemeApi(webClient)
    val editPostApi = EditPostApi(webClient, themeApi)
    val eventsApi = NotificationEventsApi(webClient)
    val favoritesApi = FavoritesApi(webClient)
    val forumApi = ForumApi(webClient)
    val mentionsApi = MentionsApi(webClient)
    val newsApi = NewsApi(webClient)
    val profileApi = ProfileApi(webClient)
    val qmsApi = QmsApi(webClient)
    val reputationApi = ReputationApi(webClient)
    val searchApi = SearchApi(webClient)
    val topicsApi = TopicsApi(webClient)

    val userSource = UserSourceProvider(qmsApi)
    val forumUsersCache = ForumUsersCache(userSource)
    val favoritesCache = FavoritesCache()
    val forumCache = ForumCache()
    val historyCache = HistoryCache()
    val qmsCache = QmsCache()

    val avatarRepository = AvatarRepository(forumUsersCache, schedulers)
    val favoritesRepository = FavoritesRepository(schedulers, favoritesApi, favoritesCache)
    val historyRepository = HistoryRepository(schedulers, historyCache)
    val mentionsRepository = MentionsRepository(schedulers, mentionsApi)
    val authRepository = AuthRepository(schedulers, authApi)
    val profileRepository = ProfileRepository(schedulers, profileApi)
    val reputationRepository = ReputationRepository(schedulers, reputationApi)
    val forumRepository = ForumRepository(schedulers, forumApi, forumCache)
    val topicsRepository = TopicsRepository(schedulers, topicsApi)
    val themeRepository = ThemeRepository(schedulers, themeApi, forumUsersCache)
    val qmsRepository = QmsRepository(schedulers, qmsApi, qmsCache, forumUsersCache)
    val searchRepository = SearchRepository(schedulers, searchApi, forumUsersCache)
    val newsRepository = NewsRepository(schedulers, newsApi)
    val devDbRepository = DevDbRepository(schedulers, devDbApi)
    val editPostRepository = PostEditorRepository(schedulers, editPostApi, forumUsersCache)
}
