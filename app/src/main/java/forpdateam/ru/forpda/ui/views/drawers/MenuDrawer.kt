package forpdateam.ru.forpda.ui.views.drawers

import android.content.Intent
import android.support.v4.app.FragmentActivity
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.ui.activities.SettingsActivity
import forpdateam.ru.forpda.ui.navigation.TabHelper
import forpdateam.ru.forpda.ui.navigation.TabNavigator
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.drawers.adapters.MenuAdapter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.*

class MenuDrawer(
        private val activity: FragmentActivity,
        private val drawerLayout: DrawerLayout,
        private val tabNavigator: TabNavigator
) {


    private val menuAdapter = MenuAdapter()
    private val menuDrawer by lazy { drawerLayout.menu_drawer }
    private val forbiddenError by lazy { drawerLayout.forbidden_error }
    private val currentMenuItems = mutableListOf<MenuItem>()
    private val router = App.get().Di().router
    private val authHolder = App.get().Di().authHolder
    private val compositeDisposable = CompositeDisposable()

    private val countsObserver = Observer { observable1, o ->
        var item = findMenuItem(Screen.QmsContacts::class.java)
        if (item != null) {
            item.notifyCount = ClientHelper.getQmsCount()
        }
        item = findMenuItem(Screen.Mentions::class.java)
        if (item != null) {
            item.notifyCount = ClientHelper.getMentionsCount()
        }
        item = findMenuItem(Screen.Favorites::class.java)
        if (item != null) {
            item.notifyCount = ClientHelper.getFavoritesCount()
        }
        menuAdapter.notifyDataSetChanged()
    }

    private val statusBarSizeObserver = Observer { observable1, o ->
        val height = App.getStatusBarHeight()
        menuDrawer.setPadding(0, height, 0, 0)
    }

    private val forbiddenObserver = Observer { o, arg ->
        activity.runOnUiThread {
            if (forbiddenError != null) {
                val isForbidden = arg as Boolean
                forbiddenError.visibility = if (isForbidden) View.VISIBLE else View.GONE
            }
        }
    }

    private val allItems = listOf(
            MenuItem(R.string.fragment_title_auth, R.drawable.ic_person_add, Screen.Auth()),
            MenuItem(R.string.fragment_title_news, R.drawable.ic_newspaper, Screen.ArticleList()),
            MenuItem(R.string.fragment_title_favorite, R.drawable.ic_star, Screen.Favorites()),
            MenuItem(R.string.fragment_title_contacts, R.drawable.ic_contacts, Screen.QmsContacts()),
            MenuItem(R.string.fragment_title_mentions, R.drawable.ic_notifications, Screen.Mentions()),
            MenuItem(R.string.fragment_title_devdb, R.drawable.ic_devices_other, Screen.DevDbBrands()),
            MenuItem(R.string.fragment_title_forum, R.drawable.ic_forum, Screen.Forum()),
            MenuItem(R.string.fragment_title_search, R.drawable.ic_search, Screen.Search()),
            MenuItem(R.string.fragment_title_history, R.drawable.ic_history, Screen.History()),
            MenuItem(R.string.fragment_title_notes, R.drawable.ic_bookmark, Screen.Notes()),
            MenuItem(R.string.fragment_title_forum_rules, R.drawable.ic_book_open, Screen.ForumRules()),
            MenuItem(R.string.activity_title_settings, R.drawable.ic_settings, Screen.Settings())
    )


    init {
        allItems.forEach { it.screen.fromMenu = true }
        drawerLayout.apply {
            menu_list.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = menuAdapter
            }
        }

        menuAdapter.setItemClickListener(object : BaseAdapter.OnItemClickListener<MenuItem> {
            override fun onItemClick(item: MenuItem) {
                openScreen(item)
                closeMenu()
            }

            override fun onItemLongClick(item: MenuItem): Boolean {
                return false
            }
        })

        App.get().addStatusBarSizeObserver(statusBarSizeObserver)
        App.get().subscribeForbidden(forbiddenObserver)
        ClientHelper.get().addCountsObserver(countsObserver)
        compositeDisposable.add(
                authHolder
                        .observe()
                        .subscribe {
                            fillMenuItems()
                            if (!it.isAuth()) {
                                ClientHelper.setQmsCount(0)
                                ClientHelper.setFavoritesCount(0)
                                ClientHelper.setMentionsCount(0)
                                ClientHelper.get().notifyCountsChanged()
                                App.get().preferences.edit().remove("menu_drawer_last").apply()
                            }
                        }
        )
        compositeDisposable.add(
                tabNavigator
                        .observeSubscribers()
                        .subscribe({
                            Log.e("lalala", "Menu subscribe")
                            it.firstOrNull { it.isActiveTab }?.also {
                                val screen = TabHelper.findScreenByFragment(it)
                                Log.e("lalala", "Menu subscribe fr=$it, sc=$screen")
                                findMenuItem(screen)?.also {
                                    Log.e("lalala", "Menu subscribe select=$it")
                                    selectMenuItem(it)
                                }
                            }
                        }, {
                            Log.d("lalala", "menu error: ${it.message}")
                        })
        )
        fillMenuItems()
    }

    private fun fillMenuItems() {
        currentMenuItems.clear()
        val isAuth = authHolder.get().isAuth()
        currentMenuItems.addAll(allItems.filter {
            val screen = it.screen
            if (isAuth) {
                screen !is Screen.Auth
            } else {
                screen !is Screen.Profile || screen !is Screen.QmsContacts || screen !is Screen.Favorites || screen !is Screen.Mentions
            }
        })
        menuAdapter.addAll(currentMenuItems)
    }

    private fun selectMenuItem(item: MenuItem) {
        currentMenuItems.forEach { it.isActive = false }
        item.isActive = true
        menuAdapter.notifyDataSetChanged()
    }

    private fun openScreen(item: MenuItem){
        router.navigateTo(item.screen)
    }

    private fun findMenuItem(classObject: Class<out Screen>): MenuItem? {
        for (item in currentMenuItems) {
            if (item.screen.javaClass == classObject)
                return item
        }
        return null
    }

    fun openMenu() {
        drawerLayout.openDrawer(menuDrawer)
    }

    fun isMenuOpen(): Boolean {
        return drawerLayout.isDrawerOpen(menuDrawer)
    }

    fun closeMenu() {
        drawerLayout.closeDrawer(menuDrawer)
    }

    fun toggleMenu() {
        if (drawerLayout.isDrawerOpen(menuDrawer)) {
            closeMenu()
        } else {
            openMenu()
        }
    }

    fun destroy() {
        App.get().removeStatusBarSizeObserver(statusBarSizeObserver)
        App.get().unSubscribeForbidden(forbiddenObserver)
        compositeDisposable.dispose()
    }
}