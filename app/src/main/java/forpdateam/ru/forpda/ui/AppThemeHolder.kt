package forpdateam.ru.forpda.ui

import io.reactivex.Observable

interface AppThemeHolder {
    fun observeTheme(): Observable<String>
    fun setTheme(theme: String)
    fun getTheme(): String
}