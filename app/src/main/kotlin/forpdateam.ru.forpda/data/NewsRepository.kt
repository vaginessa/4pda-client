package forpdateam.ru.forpda.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import forpdateam.ru.forpda.async.async
import forpdateam.ru.forpda.ext.logger
import forpdateam.ru.forpda.news.NewsHelper

/**
 * Created by isanechek on 7/2/17.
 */
class NewsRepository {

    private val newsResponse: MutableLiveData<Response<List<News>>> by lazy { MutableLiveData<Response<List<News>>>() }

    companion object {
        private val TAG: String = "NewsRepository"
        private var INSTANCE: NewsRepository? = null
        @JvmStatic
        fun createInstance() { INSTANCE = NewsRepository() }
        fun getInstance() : NewsRepository = INSTANCE ?: throw IllegalStateException("No Created NewsRepository Instance!!!")
    }

    fun loadData(request: Request): LiveData<Response<List<News>>> {
        readFromRoom(request)
        readFromNetwork(request)
        return newsResponse
    }

    fun refresh(request: Request) {
        readFromNetwork(request)
    }

    fun loadMore(request: Request) {
        readFromNetwork(request, true)
    }


    private fun readFromNetwork(request: Request, loadMore: Boolean = false) = async {
        // send action show progress
        // OldData - this is data from db
        val oldData = newsResponse.value?.data
        newsResponse.value = Response.loading(oldData, null, Response.LOAD_DATA_FROM_NETWORK)
        // async request data from network
        val result = await { NewsApi4K.getListNews(request) }
        if (result.isNotEmpty()) {
            /*
            * Шлем второй раз oldData если есть.
            * Ничего страшного. Где надо об этом знают и UI дергать не будет.))
            */
            newsResponse.value = Response.loading(oldData, null, Response.WORKING_WITH_DATA)
            // mapping data
            val response = NewsHelper.mappingNewsItemsToNews(result, request.category)
            // send data to UI
            newsResponse.value = Response.success(response, null)
        } else {
            newsResponse.value = Response.error(Response.DATA_IS_EMPTY, null)
        }

    }.onError {
        logger("Read From Network Error ${it.message}")
        newsResponse.value = Response.error(Response.DATA_IS_EMPTY_NETWORK, null)
    }

    private fun readFromRoom(request: Request) {

    }

    private fun checkNewData(oldData: List<News>?, newData: List<News>?) {

    }

    private fun load(oldData: List<News>?, newData: List<News>?) {}


}