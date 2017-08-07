package forpdateam.ru.forpda.fragments.news.main.category

import android.arch.lifecycle.ViewModel
import forpdateam.ru.forpda.data.NewsApi4K

/**
 * Created by isanechek on 8/7/17.
 */
class CategoryViewModel : ViewModel() {

    fun loadCategory() = NewsApi4K.categoryList
}