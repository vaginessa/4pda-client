package forpdateam.ru.forpda.fragments.news.details

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
import forpdateam.ru.forpda.ext.gone
import forpdateam.ru.forpda.ext.logger
import forpdateam.ru.forpda.fragments.TabFragment
import forpdateam.ru.forpda.fragments.news.details.comments.main.NewsDetailsCommentsFragment
import forpdateam.ru.forpda.fragments.news.details.content.NewsDetailsContentFragment

/**
 * Created by isanechek on 7/30/17.
 */
class NewsDetailsParentFragment : TabFragment() {

    private var prevMenuItem: MenuItem? = null

    // view's
    private lateinit var navigation: BottomNavigationView
    private lateinit var pager: ViewPager

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        baseInflateFragment(inflater, LAYOUT)
        navigation = findViewById(R.id.news_bottom_navigation) as BottomNavigationView
        navigation.inflateMenu(R.menu.news_details_bn_menu)
        pager = findViewById(R.id.news_bottom_viewpager) as ViewPager
        val detailsAdapter = PagerAdapter(childFragmentManager)
        pager.adapter = detailsAdapter
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_details_content -> pager.currentItem = 0
                R.id.action_details_comments -> pager.currentItem = 1
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
//            0 -> NewsDetailsContentFragment.createInstance(arguments)
//            1 -> NewsDetailsCommentsFragment.createInstance(arguments)
            else -> throw IllegalArgumentException("Fragment Not Found")
        }

        override fun getCount(): Int = 2

    }

    companion object {
        private val TAG = NewsDetailsParentFragment::class.java.simpleName
        private val LAYOUT: Int = R.layout.news_details_parent_layout
        const val NEWS_URL = "news.url"
        const val NEWS_IMG_URL = "news.img.url"
        const val NEWS_TITLE = "news.title"
        const val NEWS_AUTHOR = "news.author"
        const val NEWS_DATE = "news.date"
    }

    init {
        configuration.isAlone = true
        configuration.isUseCache = true
//        appBarLayout.gone()
    }
}