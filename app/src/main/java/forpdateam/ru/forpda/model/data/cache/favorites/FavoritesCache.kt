package forpdateam.ru.forpda.model.data.cache.favorites

import forpdateam.ru.forpda.entity.db.favorites.FavItemBd
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import io.realm.Realm

class FavoritesCache {

    fun getItems(): List<FavItem> = Realm.getDefaultInstance().use {
        it.where(FavItemBd::class.java).findAll().map { FavItem(it) }
    }

    fun saveFavorites(items: List<FavItem>) = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            realmTr.delete(FavItemBd::class.java)
            realmTr.copyToRealmOrUpdate(items.map { FavItemBd(it) })
        }
    }

    fun getItemByFavId(favId: Int): FavItem? = Realm.getDefaultInstance().use {
        it.where(FavItemBd::class.java).equalTo("favId", favId).findFirst()?.let {
            FavItem(it)
        }
    }

    fun getItemByTopicId(topicId: Int): FavItem? = Realm.getDefaultInstance().use {
        it.where(FavItemBd::class.java).equalTo("topicId", topicId).findFirst()?.let {
            FavItem(it)
        }
    }

    fun updateItem(item: FavItem) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(FavItemBd::class.java).equalTo("favId", item.favId).findFirst()?.let {
                realmTr.copyToRealmOrUpdate(FavItemBd(it))
            }
        }
    }

}