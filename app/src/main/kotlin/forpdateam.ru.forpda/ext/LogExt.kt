package forpdateam.ru.forpda.ext

import android.util.Log
import forpdateam.ru.forpda.BuildConfig

/**
 * Created by isanechek on 7/1/17.
 */

// из java нужно передовать оба параметра
fun logger(msg: String, error: String? = null) {
    if (BuildConfig.DEBUG) {
        when (error) {
            null -> Log.d("FORPDA_DEBUG", msg)
            else -> Log.e("FORPDA_DEBUG", msg)
        }
    }
}