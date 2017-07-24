package forpdateam.ru.forpda.news.main.timeline

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import forpdateam.ru.forpda.data.News
import forpdateam.ru.forpda.data.NewsRepository
import forpdateam.ru.forpda.data.Request
import forpdateam.ru.forpda.data.Response

/**
 * Created by isanechek on 7/6/17.
 */
open class NewsViewModel : ViewModel() {
    private val repo: NewsRepository = NewsRepository.getInstance()

    fun loadData(request: Request) : LiveData<Response<List<News>>> = repo.loadData(request)

    fun refresh(request: Request) { repo.refresh(request) }

    fun loadMore(request: Request) {

    }


}
