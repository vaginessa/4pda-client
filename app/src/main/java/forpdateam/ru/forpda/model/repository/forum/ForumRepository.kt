package forpdateam.ru.forpda.model.repository.forum

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd
import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumApi
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Realm
import java.util.*

/**
 * Created by radiationx on 03.01.18.
 */

class ForumRepository(
        private val schedulers: SchedulersProvider,
        private val forumApi: ForumApi
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

    fun markAllRead(): Observable<Any> = Observable
            .fromCallable { forumApi.markAllRead() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun markRead(id: Int): Observable<Any> = Observable
            .fromCallable { forumApi.markRead(id) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getRules(withHtml: Boolean): Observable<ForumRules> = Observable
            .fromCallable { forumApi.rules }
            .map { transform(it, withHtml) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getAnnounce(id: Int, forumId: Int, withHtml: Boolean): Observable<Announce> = Observable
            .fromCallable { forumApi.getAnnounce(id, forumId) }
            .map { transform(it, withHtml) }
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


    @Throws(Exception::class)
    fun transform(rules: ForumRules, withHtml: Boolean): ForumRules {
        if (withHtml) {
            val t = App.get().getTemplate(App.TEMPLATE_FORUM_RULES)
            App.setTemplateResStrings(t)
            t!!.setVariableOpt("style_type", App.get().cssStyleType)

            for (item in rules.items) {
                t.setVariableOpt("type", if (item.isHeader) "header" else "")
                t.setVariableOpt("number", item.number)
                t.setVariableOpt("text", item.text)
                t.addBlockOpt("rules_item")
            }

            rules.html = t.generateOutput()
            t.reset()
        }
        return rules
    }

    @Throws(Exception::class)
    fun transform(announce: Announce, withHtml: Boolean): Announce {
        if (withHtml) {
            val t = App.get().getTemplate(App.TEMPLATE_ANNOUNCE)
            App.setTemplateResStrings(t)
            t!!.setVariableOpt("style_type", App.get().cssStyleType)
            t.setVariableOpt("body", announce.html)
            announce.html = t.generateOutput()
            t.reset()
        }
        return announce
    }
}
