package forpdateam.ru.forpda.ui.views.drawers

import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentActivity
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.navigation.TabNavigator
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.drawers.adapters.TabAdapter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.*

class TabsDrawer(
        private val activity: FragmentActivity,
        private val drawerLayout: DrawerLayout,
        private val tabNavigator: TabNavigator
) {

    val tabDrawer: NavigationView by lazy { drawerLayout.tab_drawer }
    private val tabLayoutManager = LinearLayoutManager(drawerLayout.tab_list.context)
    private val tabAdapter by lazy { TabAdapter() }
    private val compositeDisposable = CompositeDisposable()

    private val preferenceObserver = Observer { observable1, o ->
        if (o == null) return@Observer
        val key = o as String
        when (key) {
            Preferences.Main.IS_TABS_BOTTOM -> {
                tabLayoutManager.stackFromEnd = Preferences.Main.isTabsBottom(activity)
            }
        }
    }

    private val statusBarSizeObserver = Observer{ observable1, o ->
        val height = App.getStatusBarHeight()
        tabDrawer.setPadding(0, height, 0, 0)
    }

    init {

        drawerLayout.apply {
            tab_list.apply {
                layoutManager = tabLayoutManager
                adapter = tabAdapter
            }
            tab_close_all.setOnClickListener { removeAllTabs() }
        }
        tabAdapter.setItemClickListener(object : BaseAdapter.OnItemClickListener<TabFragment> {
            override fun onItemClick(item: TabFragment) {
                tabNavigator.select(item.tag)
                closeTabs()
            }

            override fun onItemLongClick(item: TabFragment): Boolean {
                return false
            }
        })

        tabAdapter.setCloseClickListener(object : BaseAdapter.OnItemClickListener<TabFragment> {
            override fun onItemClick(item: TabFragment) {
                tabNavigator.close(item.tag)
            }

            override fun onItemLongClick(item: TabFragment): Boolean {
                return false
            }
        })

        val disposable = tabNavigator
                .observeSubscribers()
                .subscribe { tabAdapter.addAll(it) }
        compositeDisposable.add(disposable)

        App.get().addPreferenceChangeObserver(preferenceObserver)
        App.get().addStatusBarSizeObserver(statusBarSizeObserver)
    }

    fun openTabs() = drawerLayout.openDrawer(tabDrawer)

    fun closeTabs() = drawerLayout.closeDrawer(tabDrawer)

    fun isTabsOpen() = drawerLayout.isDrawerOpen(tabDrawer)

    fun toggleTabs() = if (isTabsOpen()) {
        closeTabs()
    } else {
        openTabs()
    }

    fun destroy() {
        tabAdapter.clear()
        compositeDisposable.dispose()
        App.get().removePreferenceChangeObserver(preferenceObserver)
        App.get().removeStatusBarSizeObserver(statusBarSizeObserver)
    }

    private fun removeAllTabs() {
        AlertDialog.Builder(activity)
                .setMessage(R.string.ask_close_other_tabs)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    tabNavigator.closeOthers()
                    closeTabs()
                }
                .setNegativeButton(R.string.no, null)
                .show()
    }
}