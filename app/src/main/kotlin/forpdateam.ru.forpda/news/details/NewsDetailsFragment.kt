package forpdateam.ru.forpda.news.details

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.data.Comment
import forpdateam.ru.forpda.data.OtherNews
import forpdateam.ru.forpda.ext.logger
import forpdateam.ru.forpda.news.NewsHelper
import forpdateam.ru.forpda.views.widgets.FixCardView

/**
 * Created by isanechek on 7/4/17.
 */
class NewsDetailsFragment : Fragment(), NewsDetailsContract.INewsDetailsView {

    private var key: String? = null

    // content view's
    // info block
    private lateinit var title: TextView
    private lateinit var date: TextView
    private lateinit var author: TextView
    private lateinit var tagsRootContainer: FixCardView
    private lateinit var tagsContainer: LinearLayout
    // content block
    private lateinit var webContainer: LinearLayout
    // more news block
    private lateinit var modeNewsList: RecyclerView
    // navigation block
    private lateinit var prevPage: ImageButton
    private lateinit var nextPage: ImageButton
    private lateinit var openComments: Button

    // comments view's

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        key = arguments.getString("_key")
        logger("News Details KEY $key", null)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // comments block
        if (key == NewsHelper.COMMENTS_TAB) {
            val commentsView = inflater?.inflate(R.layout.news_details_comments_layout, container, false)
            return commentsView
        }

        // content block
        val contentView =  inflater?.inflate(R.layout.news_details_content_layout, container, false)
        title = contentView?.findViewById(R.id.news_details_info_title) as TextView
        date = contentView.findViewById(R.id.news_details_info_date) as TextView
        author = contentView.findViewById(R.id.news_details_info_author) as TextView
        webContainer = contentView.findViewById(R.id.news_details_web_container) as LinearLayout
        return contentView
    }

    override fun showInfoBlock(url: String, title: String, author: String, data: String, category: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showContent(source: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMoreNews(items: List<OtherNews>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showComments(items: List<Comment>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    companion object {
        fun createInstance(key: String) : NewsDetailsFragment {
            val fragment = NewsDetailsFragment()
            val args = Bundle()
            args.putString("_key", key)
            fragment.arguments = args
            return fragment
        }
    }

}