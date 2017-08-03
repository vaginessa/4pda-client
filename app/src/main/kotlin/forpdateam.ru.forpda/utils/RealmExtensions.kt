package forpdateam.ru.forpda.utils

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.RealmQuery

/**
 * Original - https://github.com/vicpinm/Kotlin-Realm-Extensions
 */

/**
 * Computed variable for getting first entity in database
 */
fun <T : RealmObject> T.firstItem(configuration: RealmConfiguration): T? = Realm.getInstance(configuration)
        .use { realm ->
            val item: T? = realm.forEntity(this).findFirst()
            return if (item != null && item.isValid) realm.copyFromRealm(item) else null
        }

/**
 * Computed variable for getting all entities in database
 */
fun  <T : RealmObject> T.allItems(configuration: RealmConfiguration): List<T> = Realm.getInstance(configuration)
        .use { realm ->
            val result: List<T> = realm.forEntity(this).findAll()
            return realm.copyFromRealm(result)
        }

/**
 * Creates a new entry in database or updates an existing one. If entity has no primary key, always create a new one.
 * If has primary key, it tries to updates an existing one.
 */
fun <T : RealmObject> T.save(configuration: RealmConfiguration) {
    Realm.getInstance(configuration).transaction {
        if(this.hasPrimaryKey(it)) it.copyToRealmOrUpdate(this) else it.copyToRealm(this)
    }
}

fun <T : Collection<out RealmObject>> T.saveAll(configuration: RealmConfiguration) {
    val realm = Realm.getInstance(configuration)
    realm.transaction {
        forEach { if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
}

/**
 * Delete all entries of this type in database
 */
fun <T : RealmObject> T.deleteAll(configuration: RealmConfiguration) {
    Realm.getInstance(configuration)
            .transaction { it.forEntity(this).findAll().deleteAllFromRealm() }
}

/**
 * Delete all entries returned by the specified query
 */
fun <T : RealmObject> T.delete(myQuery: (RealmQuery<T>) -> Unit, configuration: RealmConfiguration) {
    Realm.getInstance(configuration).transaction {
        it.forEntity(this).withQuery(myQuery).findAll().deleteAllFromRealm()
    }
}

fun  Array<out RealmObject>.saveAll(configuration: RealmConfiguration) {
    val realm = Realm.getInstance(configuration)
    realm.transaction {
        forEach { if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
}

/**
 * Utility extension for modifying database. Create a transaction, run the function passed as argument,
 * commit transaction and close realm instance.
 */
fun Realm.transaction(action: (Realm) -> Unit) {
    use { executeTransaction { action(this) } }
}

private fun <T : RealmObject> Realm.forEntity(instance : T) : RealmQuery<T> = RealmQuery.createQuery(this, instance.javaClass)

private fun <T> T.withQuery(block: (T) -> Unit): T { block(this); return this }

private fun <T : RealmObject> T.hasPrimaryKey(realm : Realm) = realm.schema.get(this.javaClass.simpleName).hasPrimaryKey()