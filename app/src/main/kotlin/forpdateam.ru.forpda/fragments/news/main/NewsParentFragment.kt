package forpdateam.ru.forpda.fragments.news.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.fragments.TabFragment
import forpdateam.ru.forpda.fragments.news.main.category.NewsCategoryFragment
import forpdateam.ru.forpda.fragments.news.main.offline.NewsOfflineFragment
import forpdateam.ru.forpda.fragments.news.main.timeline.NewsTimelineFragment
import forpdateam.ru.forpda.fragments.news.main.top.NewsTopFragment
import forpdateam.ru.forpda.utils.Bus
import forpdateam.ru.forpda.utils.NetworkStatus

/**
 * Created by isanechek on 7/10/17.
 */
class NewsParentFragment : TabFragment() {

    private var prevMenuItem: MenuItem? = null
    // view's
    private lateinit var navigation: BottomNavigationView
    private lateinit var pager: ViewPager

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        baseInflateFragment(inflater, R.layout.news_main_parent_layout)
        navigation = findViewById(R.id.news_bottom_navigation) as BottomNavigationView
        navigation.inflateMenu(R.menu.news_list_bn_menu)
        pager = findViewById(R.id.news_bottom_viewpager) as ViewPager
        val adapter = PagerAdapter(childFragmentManager)
        pager.adapter = adapter
        return view
    }

    override fun loadData() {
        super.loadData()
        Bus.publish(NetworkStatus(true))
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_offline_tab -> pager.currentItem = 0
                R.id.action_list_tab -> pager.currentItem = 1
                R.id.action_category_tab -> pager.currentItem = 2
            }
            false
        }

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) prevMenuItem!!.isChecked = false
                else navigation.menu.getItem(0).isChecked = false
                navigation.menu.getItem(position).isChecked = true
                prevMenuItem = navigation.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    internal inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment = when (position) {
            0 -> NewsTimelineFragment.createInstance()
            1 -> NewsTopFragment.createInstance()
            2 -> NewsCategoryFragment.createInstance()
            3 -> NewsOfflineFragment.createInstance()
            else -> NewsTimelineFragment.createInstance()
        }

        override fun getCount(): Int = 4
    }

    init {
        configuration.isAlone = true
        configuration.isUseCache = true
        configuration.defaultTitle = "Новости"
    }
}