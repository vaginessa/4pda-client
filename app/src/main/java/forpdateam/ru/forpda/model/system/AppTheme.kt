package forpdateam.ru.forpda.model.system

import android.content.Context
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.ui.AppThemeHolder
import io.reactivex.Observable

class AppTheme(
        private val context: Context
) : AppThemeHolder {
    override fun observeTheme(): Observable<String> = BehaviorRelay.createDefault("")

    override fun setTheme(theme: String) {
    }

    override fun getTheme(): String {
        return if (Preferences.Main.Theme.isDark(context)) "dark" else "light"
    }
}