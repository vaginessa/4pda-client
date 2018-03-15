package forpdateam.ru.forpda.model.repository.forum

import java.util.ArrayList

import forpdateam.ru.forpda.api.forum.Forum
import forpdateam.ru.forpda.api.forum.models.ForumItemFlat
import forpdateam.ru.forpda.api.forum.models.ForumItemTree
import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmResults

/**
 * Created by radiationx on 03.01.18.
 */

class ForumRepository(
        private val schedulers: SchedulersProvider,
        private val forumApi: Forum
) {

    fun getForums(): Observable<ForumItemTree> = Observable
            .fromCallable { forumApi.forums }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getCache(): Observable<ForumItemTree> = Observable
            .fromCallable {
                val items = ArrayList<ForumItemFlat>()
                Realm.getDefaultInstance().use { realm ->
                    val results = realm
                            .where(ForumItemFlatBd::class.java)
                            .findAll()
                    for (itemBd in results) {
                        items.add(ForumItemFlat(itemBd))
                    }
                }
                val forumItemTree = ForumItemTree()
                forumApi.transformToTree(items, forumItemTree)
                forumItemTree
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun saveCache(rootForum: ForumItemTree): Completable = Completable
            .fromRunnable {
                Realm.getDefaultInstance().use { realm ->
                    realm.executeTransaction { r ->
                        r.delete(ForumItemFlatBd::class.java)
                        val items = ArrayList<ForumItemFlatBd>()
                        transformToList(items, rootForum)
                        r.copyToRealmOrUpdate(items)
                        items.clear()
                    }
                }
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
