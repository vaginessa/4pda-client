package forpdateam.ru.forpda

import android.content.Context
import forpdateam.ru.forpda.model.data.remote.api.auth.Auth
import forpdateam.ru.forpda.model.data.remote.api.favorites.Favorites
import forpdateam.ru.forpda.model.data.remote.api.forum.Forum
import forpdateam.ru.forpda.model.data.remote.api.mentions.Mentions
import forpdateam.ru.forpda.model.data.remote.api.profile.Profile
import forpdateam.ru.forpda.model.data.remote.api.reputation.Reputation
import forpdateam.ru.forpda.model.data.remote.api.theme.Theme
import forpdateam.ru.forpda.model.data.remote.api.topcis.Topics
import forpdateam.ru.forpda.model.NetworkStateProvider
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.model.repository.history.HistoryRepository
import forpdateam.ru.forpda.model.repository.mentions.MentionsRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
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

    private val favoritesApi = Favorites()
    private val mentionsApi = Mentions()
    private val authApi = Auth()
    private val profileApi = Profile()
    private val reputationApi = Reputation()
    private val forumApi = Forum()
    private val topicsApi = Topics()
    private val themeApi = Theme()

    val favoritesRepository = FavoritesRepository(schedulers, favoritesApi)
    val historyRepository = HistoryRepository(schedulers)
    val mentionsRepository = MentionsRepository(schedulers, mentionsApi)
    val authRepository = AuthRepository(schedulers, authApi)
    val profileRepository = ProfileRepository(schedulers, profileApi)
    val reputationRepository = ReputationRepository(schedulers, reputationApi)
    val forumRepository = ForumRepository(schedulers, forumApi)
    val topicsRepository = TopicsRepository(schedulers, topicsApi)
    val themeRepository = ThemeRepository(schedulers, themeApi)
}
