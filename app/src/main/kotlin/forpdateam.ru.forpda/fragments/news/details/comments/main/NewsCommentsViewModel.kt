package forpdateam.ru.forpda.fragments.news.details.comments.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import forpdateam.ru.forpda.data.Comment
import forpdateam.ru.forpda.data.NewsRepository
import forpdateam.ru.forpda.data.Request
import forpdateam.ru.forpda.data.Response

/**
 * Created by isanechek on 7/19/17.
 */
class NewsCommentsViewModel : ViewModel() {

    private val repository: NewsRepository = NewsRepository.getInstance()

    fun loadComments(request: Request) : LiveData<Response<List<Comment>>>  = repository.loadComments(request)

}