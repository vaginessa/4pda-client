package forpdateam.ru.forpda.data

/**
 * Created by isanechek on 8/7/17.
 */
data class Category(val title: Int, val subItems: List<SubCategory>)
data class SubCategory(val title: Int, val key: String)