package forpdateam.ru.forpda.news.details

/**
 * Created by isanechek on 7/4/17.
 */
class NewsDetailsPresenter(viewDetails: NewsDetailsContract.INewsDetailsView, keyView: String) {

    private val view: NewsDetailsContract.INewsDetailsView = viewDetails
    private val key: String = keyView

    fun loadData(id: String?) {

    }

}