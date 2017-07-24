package forpdateam.ru.forpda.data.local

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by isanechek on 7/19/17.
 * Creates the [PdaDatabase] asynchronously, exposing a LiveData object to notify of creation.
 */

object DatabaseCreator {
    val isDatabaseCreated = MutableLiveData<Boolean>()
//    lateinit var database: PdaDatabase
    private val mInitializing = AtomicBoolean(true)

    @JvmStatic
    fun createDb(context: Context) {
//        if (mInitializing.compareAndSet(true, false).not()) {
//            return
//        }
//
//        isDatabaseCreated.value = false
//
//        Completable.fromAction {
//            database = Room.databaseBuilder(context, PdaDatabase::class.java, PdaDatabase.DATABASE_NAME).build()
//        }
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ isDatabaseCreated.value = true }, {it.printStackTrace()})
    }
}