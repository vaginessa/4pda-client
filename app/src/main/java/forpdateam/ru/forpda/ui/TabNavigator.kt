package forpdateam.ru.forpda.ui

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import forpdateam.ru.forpda.presentation.Screen
import ru.terrakok.cicerone.android.SupportAppNavigator

class TabNavigator(
        activity: FragmentActivity,
        containerId: Int
) : SupportAppNavigator(activity, containerId) {

    private val tabHelper = TabHelper()

    override fun createActivityIntent(context: Context?, screenKey: String?, data: Any?): Intent? {
        return null
    }

    override fun createFragment(screenKey: String?, data: Any?): Fragment? {
        return data?.let { tabHelper.createTab(it as Screen) }
    }

}