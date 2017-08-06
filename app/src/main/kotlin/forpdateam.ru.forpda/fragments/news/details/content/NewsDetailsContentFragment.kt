package forpdateam.ru.forpda.fragments.news.details.content

import android.annotation.TargetApi
import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.data.News
import forpdateam.ru.forpda.data.Request
import forpdateam.ru.forpda.data.Response
import forpdateam.ru.forpda.ext.loadImageFromNetwork
import forpdateam.ru.forpda.ext.logger
import forpdateam.ru.forpda.fragments.news.details.NewsDetailsParentFragment
import forpdateam.ru.forpda.utils.Bus
import forpdateam.ru.forpda.utils.ExtendedWebView
import forpdateam.ru.forpda.utils.ParentCallback
import forpdateam.ru.forpda.utils.SendData
import forpdateam.ru.forpda.views.widgets.FixCardView
import io.reactivex.disposables.CompositeDisposable
import java.util.*

/**
 * Created by isanechek on 7/19/17.
 */
class NewsDetailsContentFragment : LifecycleFragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var viewModel: NewsContentViewModel
    // view's
    private lateinit var cover: AppCompatImageView
    private lateinit var toolbarTitle: AppCompatTextView
    private lateinit var toolbarSubTitle: AppCompatTextView
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var infoTitle: TextView
    private lateinit var infoAuthor: TextView
    private lateinit var infoDate: TextView
    private lateinit var webView: ExtendedWebView
    private lateinit var webViewContainer: FixCardView

    private var isWebViewReady = false
    private val actionsHandler = Handler(Looper.getMainLooper())
    private val actionsForWebView = LinkedList<Runnable>()

    private var disposable: CompositeDisposable? = null

    private var newsUrl: String? = null
    private var webClient: NewsWebViewClient? = null
    private var chromeClient: NewsChromeClient? = null

    private fun syncWithWebView(runnable: Runnable) {
        if (!isWebViewReady) {
            actionsForWebView.add(runnable)
        } else {
            actionsHandler.post(runnable)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        newsUrl = arguments.getString("news.details.url")
        disposable = CompositeDisposable()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.news_details_content_fragment_layout, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cover = view?.findViewById(R.id.news_details_content_cover) as AppCompatImageView
        toolbarTitle = view.findViewById(R.id.news_toolbar_title) as AppCompatTextView
        toolbarSubTitle = view.findViewById(R.id.news_toolbar_subtitle) as AppCompatTextView
        refresh = view.findViewById(R.id.news_details_content_refresh) as SwipeRefreshLayout
        refresh.setOnRefreshListener(this)
        infoTitle = view.findViewById(R.id.news_details_content_info_title) as TextView
        infoAuthor = view.findViewById(R.id.news_details_content_info_author) as TextView
        infoDate = view.findViewById(R.id.news_details_content_info_date) as TextView
        webViewContainer = view.findViewById(R.id.news_details_content_webview_container) as FixCardView
        webView = ExtendedWebView(activity)
        webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        webClient = NewsWebViewClient()
        webView.setWebViewClient(webClient)
        chromeClient = NewsChromeClient()
        webView.setWebChromeClient(chromeClient)
        webViewContainer.addView(webView)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NewsContentViewModel::class.java)
        if (this.arguments != null) {
            infoTitle.text = this.arguments.getString(NEWS_TITLE)
            infoAuthor.text = this.arguments.getString(NEWS_AUTHOR)
            infoDate.text = this.arguments.getString(NEWS_DATE)
            cover.loadImageFromNetwork(this.arguments.getString(NEWS_IMG_URL))
            startObserver(arguments.getString(NEWS_URL))
        } else logger("$TAG args NULL")


    }

    private fun startObserver(url: String) {
        viewModel.loadData(Request(url, null)).observe(this, android.arch.lifecycle.Observer<Response<News>> {
            it?.let { item ->

                if (item.data != null) {
                    val data = item.data
                    logger("$TAG RESULT ${data.body}")
                    webView.loadDataWithBaseURL("http://4pda.ru/", data.body, "text/html", "utf-8", null)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy()
        disposable?.dispose()
    }

    override fun onRefresh() {
//        newsUrl?.let { url -> viewModel.refresh(Request(url, null)) }
    }

    companion object {
        private val TAG = NewsDetailsContentFragment::class.java.simpleName
        fun createInstance() : NewsDetailsContentFragment = NewsDetailsContentFragment()

        const val NEWS_URL = "news.url"
        const val NEWS_IMG_URL = "news.img.url"
        const val NEWS_TITLE = "news.title"
        const val NEWS_AUTHOR = "news.author"
        const val NEWS_DATE = "news.date"
    }

    inner internal class NewsWebViewClient : WebViewClient() {
        private fun handleUri(uri : Uri) : Boolean {
            logger("$TAG NewsWebViewClient $uri")
            return false
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean =
                handleUri(Uri.parse(url))

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                handleUri(request.url)
    }

    inner internal class NewsChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
        }
    }
}