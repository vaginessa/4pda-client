package forpdateam.ru.forpda.data

/**
 * Created by isanechek on 6/19/17.
 */
//@Entity(tableName = "news")
class News {
//    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0L
    var url: String = ""
    var title: String = ""
    var description: String = ""
    var author: String = ""
    var date: String = ""
    var category: String = ""
    var imgUrl: String = ""
    var commentsCount: String = ""
    var tags: String = ""

    // for details
    var body: String = ""
    var moreNews: String = ""
    var navId: String = ""
    var comments: String = ""
    var lastUpdate: Long = 0L


    // other
    var offline: Boolean = false
    var favorite: Boolean = false
    var newsNew: Boolean = false
    var read: Boolean = false

    // life
    var lastRequestDate: Long = 0

    companion object {
        const val NEWS_TABLE_NAME = "news"
    }
}

// for network
// две модели чтобы отделить сетевую часть
class NewsItem {
    var url: String = ""
    var title: String = ""
    var description: String = ""
    var author: String = ""
    var date: String = ""
    var imgUrl: String = ""
    var commentsCount: String = ""
    var tags: String = ""
}
// для блока - новости по теме
class OtherNews {
    var url: String = ""
    var title: String = ""
    var imgUrl: String = ""
    // for top comments news
    var commentsCount: String = ""
}