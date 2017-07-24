//package forpdateam.ru.forpda.data.local
//
//import android.arch.persistence.room.Database
//import android.arch.persistence.room.RoomDatabase
//import forpdateam.ru.forpda.data.News
//import forpdateam.ru.forpda.data.local.dao.NewsDao
//
///**
// * Created by isanechek on 7/19/17.
// */
//@Database(entities = arrayOf(News::class), version = 2)
//abstract class PdaDatabase : RoomDatabase() {
//    abstract fun repoDao(): NewsDao
//
//    companion object {
//        const val DATABASE_NAME = "forpda.db"
//    }
//}