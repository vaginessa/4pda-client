package forpdateam.ru.forpda.ui

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.ui.fragments.TabFragment
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.commands.*

class TabNavigator(
        private val activity: FragmentActivity,
        private val containerId: Int
) : Navigator {

    companion object {
        private const val TAG_PREFIX = "Tab_"
    }

    class TabItem {
        var tag: String = ""
        var screen: String = ""
        var parent: TabItem? = null
        val childs = mutableListOf<TabItem>()
    }


    class TabItemController {
        private var currentTag = ""
        private val tabs = mutableListOf<TabItem>()

        fun getCurrent() = findTabItem(currentTag)

        fun addNew(tag: String, screen: String): TabItem {
            val item = findTabItem(currentTag)
            val newItem = TabItem().also {
                it.tag = tag
                it.screen = screen
            }
            if (item != null) {
                item.childs.add(newItem.apply {
                    parent = item
                })
            } else {
                tabs.add(newItem)
            }
            currentTag = tag
            Log.e("TabController", "addNew t=$tag, s=$screen")
            printTabItems()
            return newItem
        }

        fun remove(tag: String, print: Boolean = true) {
            findTabItem(tag)?.also { item ->
                item.parent?.also { parent ->
                    val index = parent.childs.indexOf(item)
                    parent.childs.removeAt(index)
                    parent.childs.addAll(index, item.childs)
                    item.childs.forEach { child ->
                        child.parent = parent
                    }
                    item.childs.clear()
                    currentTag = parent.tag
                }
                item.parent = null
            }
            Log.e("TabController", "remove t=$tag")
            if (print) {
                printTabItems()

            }
        }

        fun replace(tag: String, screen: String) {
            findTabItem(currentTag)?.also { item ->
                val newItem = TabItem().also {
                    it.tag = tag
                    it.screen = screen
                }
                item.parent?.also { parent ->
                    val index = parent.childs.indexOf(item)
                    parent.childs.removeAt(index)
                    parent.childs.addAll(index, item.childs)
                    parent.childs.add(index, newItem.also {
                        it.parent = parent
                    })
                    item.childs.forEach { child ->
                        child.parent = parent
                    }
                    item.childs.clear()
                }
                item.parent = null
            }
            currentTag = tag
            Log.e("TabController", "replace t=$tag, s=$screen")
            printTabItems()
        }

        fun backTo(screen: String): List<String> {
            val tagsRemove = mutableListOf<String>()
            findTabItem(currentTag)?.let { item ->
                var parent: TabItem? = item
                while (parent != null) {
                    if (parent.screen == screen) {
                        break
                    }
                    tagsRemove.add(parent.tag)
                    parent = parent.parent
                }
                tagsRemove.forEach { remove(it, false) }
            }
            Log.e("TabController", "backTo s=$screen")
            printTabItems()
            return tagsRemove
        }

        private fun findTabItem(tag: String): TabItem? {
            tabs.forEach {
                if (it.tag == tag) {
                    return it
                }
                findTabItemTree(it, tag)?.also {
                    return it
                }
            }
            return null
        }

        private fun findTabItemTree(tab: TabItem, tag: String): TabItem? {
            tab.childs.forEach {
                if (it.tag == tag) {
                    return it
                }
                findTabItemTree(it, tag)?.also {
                    return it
                }
            }
            return null
        }

        private fun printTabItems() {
            var lal = ""
            tabs.forEach {
                lal += "root->TabItem(${it.tag}, ${it.screen}, ${it.parent?.tag}, ${it.childs.size})${if (currentTag == it.tag) " <-- current" else ""}\n"
                lal += printTabItemsTree(it, 1)
            }
            System.out.print(lal)
            Log.e("TabController", "tree:\n$lal")
        }

        private fun printTabItemsTree(tab: TabItem, level: Int): String {
            var lal = ""
            tab.childs.forEach {
                lal += "      "
                for (i in 1 until level) {
                    lal += "+--"
                }
                lal += "+->TabItem(${it.tag}, ${it.screen}, ${it.parent?.tag}, ${it.childs.size})${if (currentTag == it.tag) " <-- current" else ""}\n"
                lal += printTabItemsTree(it, level + 1)
            }
            return lal
        }
    }

    private val tabHelper = TabHelper()
    private val fragmentManager = activity.supportFragmentManager
    private val existing = mutableListOf<TabFragment>()
    private val tabController = TabItemController()


    private fun updateFragmentList() {
        existing.clear()
        fragmentManager.fragments?.let {
            existing.addAll(it.map { it as TabFragment })
        }
        existing.sortBy { it.tag }
    }

    private fun updateFragmentsState() {
        fragmentManager?.fragments?.let {
            it.forEach {
                Log.e("TabNavigator", "FrItem: $it")
            }
        }
        tabController.getCurrent()?.tag?.let { getByTag(it) }?.also { fragment ->
            fragmentManager
                    .beginTransaction()
                    .show(fragment)
                    .commit()
        }
    }

    private fun getByTag(tag: String): TabFragment? {
        val result = fragmentManager.findFragmentByTag(tag) as TabFragment?
        Log.e("TabNavigator", "getByTag tag=$tag, tab=${tabController.getCurrent()?.tag}, fr=$result")
        return result
    }

    private fun genTag() = TAG_PREFIX + System.currentTimeMillis()

    override fun applyCommands(commands: Array<out Command>) {
        fragmentManager.executePendingTransactions()
        commands.forEach {
            applyCommand(it)
        }
    }

    private fun applyCommand(command: Command) {
        when (command) {
            is Forward -> forward(command)
            is Back -> back()
            is Replace -> replace(command)
            is BackTo -> backTo(command)
            is SystemMessage -> showSystemMessage(command.message)
        }
        updateFragmentList()
    }

    private fun forward(command: Forward) {
        val newFragment = createFragment(command.screenKey, command.transitionData)
        if (newFragment != null) {
            val tag = genTag()

            Log.e("TabNavigator", "forward f=$newFragment")
            fragmentManager
                    .beginTransaction()
                    .add(containerId, newFragment, tag)
                    .commit()
            tabController.addNew(tag, (command.transitionData as Screen).getKey())
            updateFragmentsState()
        }
    }

    private fun back() {
        val tab = tabController.getCurrent()
        if (tab == null) {
            exit()
        } else {
            val fragment = getByTag(tab.tag)

            Log.e("TabNavigator", "back f=$fragment")
            fragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commit()
            tabController.remove(tab.tag)
            updateFragmentsState()
        }
    }

    private fun replace(command: Replace) {
        val newFragment = createFragment(command.screenKey, command.transitionData)
        if (newFragment != null) {
            val tag = genTag()
            val fragment = getByTag(tabController.getCurrent()?.tag.orEmpty())
            Log.e("TabNavigator", "replace nf=$newFragment, of=$fragment")
            fragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .add(containerId, newFragment, tag)
                    .commit()
            updateFragmentsState()
            tabController.replace(tag, (command.transitionData as Screen).getKey())
        }
    }

    private fun backTo(command: BackTo) {
        val tagsRemove = tabController.backTo(command.screenKey)
        val transaction = fragmentManager.beginTransaction()
        Log.e("TabNavigator", "backTo tags=${tagsRemove.size}")
        tagsRemove.forEach {
            val fragment = getByTag(it)
            Log.e("TabNavigator", "backTo remove t=$fragment")
            transaction.remove(fragment)
        }
        transaction.commit()
        updateFragmentsState()
    }


    fun exit() {
        activity.finish()
    }

    fun showSystemMessage(message: String) {

    }

    fun createActivityIntent(context: Context?, screenKey: String?, data: Any?): Intent? {
        return null
    }

    private fun createFragment(screenKey: String?, data: Any?): Fragment? {
        return data?.let { tabHelper.createTab(it as Screen) }
    }
}