package forpdateam.ru.forpda.news.details.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.base.BaseLifecycleFragment
import forpdateam.ru.forpda.utils.ExtendedWebView

/**
 * Created by isanechek on 7/19/17.
 */
class NewsDetailsContentFragment : BaseLifecycleFragment<NewsContentViewModel>() {

    private var webView: ExtendedWebView? = null
    private var webViewClient: WebViewClient? = null
    private var chromeClient: WebChromeClient? = null
    private var isWebViewReady = false

    override val viewModelClass = NewsContentViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater?.inflate(R.layout.news_details_content_layout, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel.
    }

}