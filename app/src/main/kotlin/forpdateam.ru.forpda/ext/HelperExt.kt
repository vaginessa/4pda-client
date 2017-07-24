package forpdateam.ru.forpda.ext

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.view.View

/**
 * Created by isanechek on 7/7/17.
 */

inline fun <reified T: ViewModel> ViewModelProvider.get (): T {
    return this.get(T::class.java)
}

inline fun View.visible() {
    visibility = View.VISIBLE
}

inline fun View.invisible() {
    visibility = View.INVISIBLE
}

inline fun View.gone() {
    visibility = View.GONE
}