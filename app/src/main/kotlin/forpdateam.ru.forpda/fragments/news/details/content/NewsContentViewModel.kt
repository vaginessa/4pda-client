package forpdateam.ru.forpda.fragments.news.details.content

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import forpdateam.ru.forpda.data.News
import forpdateam.ru.forpda.data.NewsRepository
import forpdateam.ru.forpda.data.Request
import forpdateam.ru.forpda.data.Response

/**
 * Created by isanechek on 7/19/17.
 */
class NewsContentViewModel : ViewModel() {

    private val repository: NewsRepository = NewsRepository.getInstance()

    fun loadData(request: Request) : LiveData<Response<News>> = repository.loadDetailsData2(request)

    fun refresh(request: Request) {

    }

}