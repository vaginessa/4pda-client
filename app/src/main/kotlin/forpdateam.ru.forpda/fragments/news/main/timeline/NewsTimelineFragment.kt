package forpdateam.ru.forpda.fragments.news.main.timeline

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.TabManager
import forpdateam.ru.forpda.api.news.Constants
import forpdateam.ru.forpda.data.News
import forpdateam.ru.forpda.data.Request
import forpdateam.ru.forpda.data.Response
import forpdateam.ru.forpda.data.Status
import forpdateam.ru.forpda.ext.gone
import forpdateam.ru.forpda.ext.logger
import forpdateam.ru.forpda.ext.visible
import forpdateam.ru.forpda.fragments.TabFragment
import forpdateam.ru.forpda.fragments.news.details.NewsDetailsParentFragment
import forpdateam.ru.forpda.pref.Preferences
import forpdateam.ru.forpda.views.widgets.FixCardView
import forpdateam.ru.forpda.views.widgets.RecyclerViewHeader

/**
 * Created by isanechek on 7/1/17.
 */
class NewsTimelineFragment : LifecycleFragment(), NewsAdapter.ItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var pref = Preferences.News()
    private var pageSize = 1

    private lateinit var viewModel: NewsViewModel
    private lateinit var rootAdapter: NewsAdapter

    // view's
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var rootList: RecyclerView
    private lateinit var header: RecyclerViewHeader
    private lateinit var progressContainer: LinearLayout
    private lateinit var progress: ProgressBar
    private lateinit var progressTv: TextView
    private lateinit var progressIv: ImageView

    // top header
    private lateinit var tv1: TextView
    private lateinit var tv2: TextView
    private lateinit var tv3: TextView
    private lateinit var topListContainer: FixCardView
    private lateinit var topList: RecyclerView
    private lateinit var topAdapter: NewsTopAdapter



    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.news_list_layout, container, false)
        view?.let {
            // setup progress view's
            progressContainer = it.findViewById(R.id.news_list_progress_container) as LinearLayout
            progress = it.findViewById(R.id.news_list_progress) as ProgressBar
            progressTv = it.findViewById(R.id.news_list_progress_tv) as TextView
            progressIv = it.findViewById(R.id.news_list_progress_iv) as ImageView
            // setup content view's
            refresh = it.findViewById(R.id.news_refresh_layout) as SwipeRefreshLayout
            rootList = it.findViewById(R.id.news_list) as RecyclerView
            // setup top header view's
            header = it.findViewById(R.id.news_view_header_layout) as RecyclerViewHeader
        }
        viewModel = ViewModelProviders.of(this).get(NewsViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootList.layoutManager = LinearLayoutManager(activity)
        rootList.setHasFixedSize(true)
        rootAdapter = NewsAdapter()
        rootAdapter.setClickListener(this)
        rootList.adapter = rootAdapter
//        if (pref.showComments) {
//            header.visibility = View.VISIBLE
//            header.attachTo(rootList)
//            view?.let { initHeaderView(it) }
//        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel
                .loadData(Request(null, Constants.NEWS_CATEGORY_ALL))
                .observe(this, Observer<Response<List<News>>> { response ->
                    response?. let {
                        when {
                            response.status == Status.LOADING -> when {
                                response.data == null -> updateStatusProgress(response.message)
                                else -> backgroundUpdate(response.data)
                            }
                            response.status == Status.ERROR -> when {
                                response.data == null -> showErrorMessage(response.message)
                                else -> {
                                    setupRootList(response.data)
                                    showErrorMessage(response.message, true)
                                }
                            }
                            response.status == Status.SUCCESS -> {
                                progress.gone()
                                progressTv.gone()
                                progressContainer.gone()
                                setupRootList(response.data)
                            }
                        }
                    }
                })
    }

    private fun showErrorMessage(message: String?, toast: Boolean = false) {

        if (!toast) {
            progress.gone()
            when (message) {
                Response.DATA_IS_EMPTY_NETWORK -> progressTv.text = getString(R.string.downloads_network_error)
                Response.DATA_IS_EMPTY -> progressTv.text = getString(R.string.working_with_data_error)
            }
        } else {
            // hide background update progress

            // show toast
            when (message) {
                Response.DATA_IS_EMPTY_NETWORK -> Toast.makeText(activity, getString(R.string.downloads_network_error), Toast.LENGTH_SHORT).show()
                Response.DATA_IS_EMPTY -> Toast.makeText(activity, getString(R.string.working_with_data_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRootList(items: List<News>?) {

        items?.let { rootAdapter.insertAll(it) }
        if (refresh.visibility == View.GONE) refresh.visible()
    }

    private fun backgroundUpdate(items: List<News>?) {
        // show progress (later)

        // send data to list
        setupRootList(items)
    }

    private fun updateStatusProgress(message: String?) {
        when (message) {
            Response.LOAD_DATA_FROM_NETWORK -> {
                progressContainer.visible()
                progressTv.visible()
                progressTv.text = getString(R.string.downloads_from_network_text)
                progress.visible()
            }
            Response.WORKING_WITH_DATA -> progressTv.text = getString(R.string.working_with_data_text)
        }
    }

    override fun onRefresh() {
        viewModel.refresh(Request(null, ""))
    }

    override fun itemClick(itemView: View, position: Int) {
        openDetailsFragment(itemView, position)
    }

    override fun itemLongClick(position: Int) {
    }

    private fun initHeaderView(view: View) {
        tv1 = view.findViewById(R.id.news_top_header_tv1) as TextView
        tv2 = view.findViewById(R.id.news_top_header_tv2) as TextView
        tv3 = view.findViewById(R.id.news_top_header_tv3) as TextView
        topListContainer = view.findViewById(R.id.news_top_comments_list_container) as FixCardView
        topList = view.findViewById(R.id.news_top_comments_list) as RecyclerView
        topList.layoutManager = LinearLayoutManager(activity)
        topList.setHasFixedSize(true)
        topAdapter = NewsTopAdapter()
        topAdapter.setOnClickItemListener(object : NewsTopAdapter.ItemClickListener {
            override fun itemClick(itemView: View, position: Int) {
                openDetailsFragment(itemView, position)
            }

            override fun itemLongClick(position: Int) {
            }

        })
        topList.adapter = topAdapter
    }

    private fun openDetailsFragment(itemView: View, position: Int) {
        val model = rootAdapter.getItem(position)
        model.url.let {
            val args = Bundle()
            args.putString(NewsDetailsParentFragment.NEWS_IMG_URL, model.imgUrl)
            args.putString(NewsDetailsParentFragment.NEWS_TITLE, model.title)
            args.putString(NewsDetailsParentFragment.NEWS_URL, model.url)
            args.putString(NewsDetailsParentFragment.NEWS_AUTHOR, model.author)
            args.putString(NewsDetailsParentFragment.NEWS_DATE, model.date)
            TabManager.getInstance().add(TabFragment.Builder(NewsDetailsParentFragment::class.java).setArgs(args).build())
        }
    }

    companion object {
        private val TAG = NewsTimelineFragment::class.java.simpleName
        fun createInstance() : NewsTimelineFragment = NewsTimelineFragment()
    }

}