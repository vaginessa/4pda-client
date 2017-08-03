package forpdateam.ru.forpda.fragments.news.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.MenuItem
import android.view.View
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.base.BaseParentFragment
import forpdateam.ru.forpda.fragments.news.main.category.NewsCategoryFragment
import forpdateam.ru.forpda.fragments.news.main.offline.NewsOfflineFragment
import forpdateam.ru.forpda.fragments.news.main.timeline.NewsTimelineFragment
import forpdateam.ru.forpda.fragments.news.main.top.NewsTopFragment

/**
 * Created by isanechek on 7/10/17.
 */
class NewsParentFragment : BaseParentFragment() {
    override fun getMenuId(): Int = R.menu.news_list_bn_menu

    private var prevMenuItem: MenuItem? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pager.adapter = PagerAdapter(childFragmentManager)
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