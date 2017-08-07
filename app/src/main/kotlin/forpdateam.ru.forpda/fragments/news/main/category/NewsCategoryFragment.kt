package forpdateam.ru.forpda.fragments.news.main.category

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.base.BaseLifecycleFragment
import forpdateam.ru.forpda.data.NewsApi4K
import forpdateam.ru.forpda.ext.logger

/**
 * Created by isanechek on 7/10/17.
 */
class NewsCategoryFragment : Fragment(), CategoryAdapter.ClickItemListener {

    private lateinit var list: RecyclerView
    private lateinit var adapter: CategoryAdapter


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.news_category_fragment_layout, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = view?.findViewById(R.id.news_category_list) as RecyclerView
        list.layoutManager = LinearLayoutManager(activity)
        list.setHasFixedSize(true)
        adapter = CategoryAdapter()
        adapter.setOnClickListener(this)
        val list = NewsApi4K.categoryList
        logger("$TAG HUYAK SIZE ${list.size}")
        adapter.insertItems(list)
    }

    override fun clickItem(category: String) {
        logger("$TAG Click category $category")
    }

    companion object {
        private val TAG = NewsCategoryFragment::class.java.simpleName
        fun createInstance() : NewsCategoryFragment = NewsCategoryFragment()
    }

    init {
        logger("$TAG HUYAK HUYAK")
    }
}