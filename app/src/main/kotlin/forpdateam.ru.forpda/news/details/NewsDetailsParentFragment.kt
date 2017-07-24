package forpdateam.ru.forpda.news.details

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.fragments.TabFragment
import forpdateam.ru.forpda.news.NewsHelper

/**
 * Created by isanechek on 7/4/17.
 */
class NewsDetailsParentFragment : TabFragment() {

    private var prevMenuItem: MenuItem? = null

    // view's
    private lateinit var navigation: BottomNavigationView
    private lateinit var pager: ViewPager

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        baseInflateFragment(inflater, R.layout.news_details_parent_layout)
        navigation = findViewById(R.id.news_bottom_navigation) as BottomNavigationView

        navigation.inflateMenu(R.menu.menu_bottom_navigation)
        pager = findViewById(R.id.news_bottom_viewpager) as ViewPager
        val adapter = PagerAdapter(childFragmentManager)
        pager.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_news -> pager.currentItem = 0
                R.id.action_comments -> pager.currentItem = 1
            }
            false
        }

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when {
                    prevMenuItem != null -> prevMenuItem!!.isChecked = false
                    else -> navigation.menu.getItem(0).isChecked = false
                }
                navigation.menu.getItem(position).isChecked = true
                prevMenuItem = navigation.menu.getItem(position)

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    internal inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        private val fragments = listOf(NewsHelper.NEWS_TAB, NewsHelper.COMMENTS_TAB)

        override fun getItem(position: Int): Fragment {
            return NewsDetailsFragment.createInstance(fragments[position])
        }

        override fun getCount(): Int {
            return fragments.size
        }

    }

    companion object {
        fun createInstance(id: String) : NewsDetailsParentFragment {
            val fragment = NewsDetailsParentFragment()
            val args = Bundle()
            args.putString("_id", id)
            fragment.arguments = args
            return fragment
        }
    }
}