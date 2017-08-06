package forpdateam.ru.forpda.pref

import forpdateam.ru.forpda.App

/**
 * Created by isanechek on 5/11/17.
 */

open class Preferences {

    open class News {
        open var compatItem: Boolean
            get() = App.getInstance().preferences.getBoolean("news.list.compat", true)
            set(value) = App.getInstance().preferences.edit().putBoolean("news.list.compat", value).apply()

        open var showTopCommentsNew: Boolean
            get() = App.getInstance().preferences.getBoolean("news.list.top", true)
            set(value) = App.getInstance().preferences.edit().putBoolean("news.list.top", value).apply()

        open var showComments: Boolean
            get() = App.getInstance().preferences.getBoolean("news.comments", true)
            set(value) = App.getInstance().preferences.edit().putBoolean("news.comments", value).apply()
    }

    open class Media {
        open var playGif: Boolean
            get() = App.getInstance().preferences.getBoolean("media.enable.play.gif", true)
            set(value) = App.getInstance().preferences.edit().putBoolean("media.enable.play.gif", value).apply()
    }
}
