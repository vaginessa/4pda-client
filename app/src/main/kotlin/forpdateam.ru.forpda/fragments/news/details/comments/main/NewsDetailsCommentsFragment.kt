package forpdateam.ru.forpda.fragments.news.details.comments.main

import android.arch.lifecycle.LifecycleFragment
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import forpdateam.ru.forpda.R

/**
 * Created by isanechek on 7/19/17.
 */
class NewsDetailsCommentsFragment : LifecycleFragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var list: RecyclerView
    private lateinit var header: View

    private lateinit var adapter: NewsCommentsAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.news_details_comments_layout, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh = view?.findViewById(R.id.news_details_comments_refresh) as SwipeRefreshLayout
        refresh.setOnRefreshListener(this)
        refresh.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorAccent))
        list = view.findViewById(R.id.news_details_comments_list) as RecyclerView
        list.layoutManager = LinearLayoutManager(activity)
        header = getLayoutInflater(savedInstanceState).inflate(R.layout.news_comments_header_layout, list, false)
        adapter = NewsCommentsAdapter(header)
        list.adapter = adapter

    }

    override fun onRefresh() {

    }

    companion object {
        fun createInstance(args: Bundle) : NewsDetailsCommentsFragment = NewsDetailsCommentsFragment()
    }
}