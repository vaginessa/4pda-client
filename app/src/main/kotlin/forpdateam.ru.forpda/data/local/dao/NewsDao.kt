//package forpdateam.ru.forpda.data.local.dao
//
//import android.arch.persistence.room.Dao
//import android.arch.persistence.room.Insert
//import android.arch.persistence.room.OnConflictStrategy
//import android.arch.persistence.room.Query
//import forpdateam.ru.forpda.data.News
//import io.reactivex.Flowable
//
///**
// * Created by isanechek on 7/19/17.
// */
//@Dao
//interface NewsDao {
//
//    @Query("SELECT * FROM news WHERE category = :category")
//    fun loadNews(category: String): Flowable<List<News>>
//
//    @Query("SELECT * FROM news WHERE _id = :id")
//    fun loadNews(id: Long): Flowable<News>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertNews(item: News): Unit
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertNews(items: List<News>): Unit
//}