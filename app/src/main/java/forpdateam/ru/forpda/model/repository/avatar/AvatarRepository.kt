package forpdateam.ru.forpda.model.repository.avatar

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.search.SearchApi
import io.reactivex.Observable

/**
 * Created by radiationx on 01.01.18.
 */

class AvatarRepository(
        private val forumUsersCache: ForumUsersCache,
        private val schedulers: SchedulersProvider
) {

    fun getAvatar(id: Int, nick: String): Observable<String> = Observable
            .fromCallable {
                getAvatarSync(id, nick) ?: throw NullPointerException("No avatar/user by id: $id")
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getAvatar(id: Int): Observable<String> = Observable
            .fromCallable {
                getAvatarSync(id) ?: throw NullPointerException("No avatar/user by id: $id")
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getAvatar(nick: String): Observable<String> = Observable
            .fromCallable {
                getAvatarSync(nick) ?: throw NullPointerException("No avatar/user by nick: $nick")
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getAvatarSync(id: Int, nick: String): String? {
        val forumUser = forumUsersCache.getUserById(id)
                ?: forumUsersCache.getUserByNick(nick)
        return forumUser?.avatar
    }

    fun getAvatarSync(id: Int): String? = forumUsersCache.getUserById(id)?.avatar

    fun getAvatarSync(nick: String): String? = forumUsersCache.getUserByNick(nick)?.avatar

}
