package forpdateam.ru.forpda.ui.navigation

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.ui.fragments.TabFragment
import io.reactivex.Observable
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.commands.*

class TabNavigator(
        private val activity: FragmentActivity,
        private val containerId: Int
) : Navigator {

    companion object {
        private const val TAG_PREFIX = "Tab_"
    }

    private val tabHelper by lazy { TabHelper() }
    private val fragmentManager by lazy { activity.supportFragmentManager }
    private val tabController by lazy { TabController() }

    private val subscribers = mutableListOf<TabFragment>()
    private val subscribersRelay = BehaviorRelay.createDefault(subscribers as List<TabFragment>)

    fun subscribe(tab: TabFragment) {
        Log.e("lalala", "subscribe $tab")
        subscribers.add(tab)
        subscribersRelay.accept(subscribers)
    }

    fun unsubscribe(tab: TabFragment) {
        Log.e("lalala", "unsubscribe $tab")
        subscribers.remove(tab)
        subscribersRelay.accept(subscribers)
    }

    fun notifyUpdate(tab: TabFragment) {
        Log.e("lalala", "notifyUpdate $tab")
        subscribersRelay.accept(subscribers)
    }

    fun observeSubscribers(): Observable<List<TabFragment>> = subscribersRelay

    fun select(tabTag: String?) {
        if (tabTag == null) {
            Log.e("lalala", "select CANCEL: tabTag==null")
            return
        }
        val fragment = getByTag(tabTag)
        Log.e("lalala", "select tag=$tabTag fr=$fragment")
        tabController.setCurrent(tabTag)
        updateFragmentsState()
    }

    fun close(tabTag: String?) {
        if (tabTag == null) {
            Log.e("lalala", "close CANCEL: tabTag==null")
            return
        }
        val fragment = getByTag(tabTag)
        Log.e("lalala", "close tag=$tabTag fr=$fragment")
        fragmentManager
                .beginTransaction()
                .remove(fragment)
                .commit()
        tabController.remove(tabTag)
        updateFragmentsState()
    }

    fun closeOthers() {
        val transaction = fragmentManager.beginTransaction()
        val itemTags = tabController.getList().map { it.tag }.filter { it != tabController.getCurrent()?.tag }
        Log.e("lalala", "closeOthers")
        itemTags.forEach { itemTag ->
            getByTag(itemTag)?.also { fragment ->
                Log.e("lalala", "closeOthers item=${itemTag} fr=$fragment")
                transaction.remove(fragment)
                tabController.remove(itemTag)
            }
        }
        transaction.commit()
        updateFragmentsState()
    }

    private fun updateFragmentsState() {
        /*tabController.getCurrent()?.tag?.let { getByTag(it) }?.also { fragment ->
            fragmentManager
                    .beginTransaction()
                    .show(fragment)
                    .commit()
        }*/
        tabController.printTabItems("lalala")
        Log.e("lalala", "updateFragmentsState")
        val transaction = fragmentManager.beginTransaction()
        tabController.getList().forEach { item ->
            getByTag(item.tag)?.also { fragment ->
                Log.e("lalala", "updateFragmentsState item=${item.tag} ct=${tabController.getCurrent()?.tag} fr=${fragment} ")
                if (item.tag == tabController.getCurrent()?.tag) {
                    transaction.show(fragment)
                } else {
                    transaction.hide(fragment)
                }
            }
        }
        transaction.commit()
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
            tabController.replace(tag, (command.transitionData as Screen).getKey())
            updateFragmentsState()
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