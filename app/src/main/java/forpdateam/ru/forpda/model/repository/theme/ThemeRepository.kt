package forpdateam.ru.forpda.model.repository.theme

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import io.reactivex.Observable

/**
 * Created by radiationx on 15.03.18.
 */

class ThemeRepository(
        private val schedulers: SchedulersProvider,
        private val themeApi: ThemeApi,
        private val historyCache: HistoryCache,
        private val forumUsersCache: ForumUsersCache
) {

    fun getTheme(url: String, withHtml: Boolean, hatOpen: Boolean, pollOpen: Boolean): Observable<ThemePage> = Observable
            .fromCallable { themeApi.getTheme(url, hatOpen, pollOpen) }
            .doOnNext {
                saveUsers(it)
                historyCache.add(it.id, it.url, it.title)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun reportPost(themeId: Int, postId: Int, message: String): Observable<Boolean> = Observable
            .fromCallable { themeApi.reportPost(themeId, postId, message) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deletePost(postId: Int): Observable<Boolean> = Observable
            .fromCallable { themeApi.deletePost(postId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun votePost(postId: Int, type: Boolean): Observable<String> = Observable
            .fromCallable { themeApi.votePost(postId, type) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    private fun saveUsers(page: ThemePage) {
        val forumUsers = page.posts.map { post ->
            ForumUser().apply {
                id = post.userId
                nick = post.nick
                avatar = post.avatar
            }
        }
        forumUsersCache.saveUsers(forumUsers)
    }
}
