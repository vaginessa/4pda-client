package forpdateam.ru.forpda.base

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ext.gone
import forpdateam.ru.forpda.fragments.TabFragment

/**
 * Created by isanechek on 8/3/17.
 */
abstract class BaseParentFragment : TabFragment() {

    protected lateinit var navigation: BottomNavigationView
    protected lateinit var pager: ViewPager

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        appBarLayout.gone()
        baseInflateFragment(inflater, R.layout.bottom_navigation_layout)
        navigation = findViewById(R.id.news_bottom_navigation) as BottomNavigationView
        navigation.inflateMenu(getMenuId())
        pager = findViewById(R.id.news_bottom_viewpager) as ViewPager
        return view
    }

    protected abstract fun getMenuId(): Int
}