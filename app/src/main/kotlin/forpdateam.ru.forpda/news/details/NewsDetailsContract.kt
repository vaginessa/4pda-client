package forpdateam.ru.forpda.news.details

import forpdateam.ru.forpda.data.Comment
import forpdateam.ru.forpda.data.OtherNews

/**
 * Created by isanechek on 7/4/17.
 */

class NewsDetailsContract {

    interface INewsDetailsView {
        fun showInfoBlock(url: String,
                          title: String,
                          author: String,
                          data: String,
                          category: String?)

        fun showContent(source: String)
        fun showMoreNews(items: List<OtherNews>)
        fun showComments(items: List<Comment>)
    }

    interface INewsDetailsPresenter {
        fun bind(view: INewsDetailsView)
        fun unbind()
    }
}
