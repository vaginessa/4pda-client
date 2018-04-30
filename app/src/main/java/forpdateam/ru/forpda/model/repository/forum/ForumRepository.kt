package forpdateam.ru.forpda.model.repository.forum

import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd
import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forum.ForumCache
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumApi
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Created by radiationx on 03.01.18.
 */

class ForumRepository(
        private val schedulers: SchedulersProvider,
        private val forumApi: ForumApi,
        private val forumCache: ForumCache
) {

    fun getForums(): Observable<ForumItemTree> = Observable
            .fromCallable { forumApi.forums }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getCache(): Observable<ForumItemTree> = Observable
            .fromCallable {
                ForumItemTree().apply {
                    forumApi.transformToTree(forumCache.getItems(), this)
                }
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun markAllRead(): Observable<Any> = Observable
            .fromCallable { forumApi.markAllRead() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun markRead(id: Int): Observable<Any> = Observable
            .fromCallable { forumApi.markRead(id) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getRules(): Observable<ForumRules> = Observable
            .fromCallable { forumApi.rules }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getAnnounce(id: Int, forumId: Int): Observable<Announce> = Observable
            .fromCallable { forumApi.getAnnounce(id, forumId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun saveCache(rootForum: ForumItemTree): Completable = Completable
            .fromRunnable {
                val items = mutableListOf<ForumItemFlatBd>().apply {
                    transformToList(this, rootForum)
                }
                forumCache.saveItems(items)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    private fun transformToList(list: MutableList<ForumItemFlatBd>, rootForum: ForumItemTree) {
        if (rootForum.forums == null) return
        for (item in rootForum.forums) {
            list.add(ForumItemFlatBd(item))
            transformToList(list, item)
        }
    }

}
