package forpdateam.ru.forpda.model.repository.theme

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.apirx.ForumUsersCache
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostApi
import forpdateam.ru.forpda.model.repository.temp.TempHelper
import io.reactivex.Observable
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 15.03.18.
 */

class ThemeRepository(
        private val schedulers: SchedulersProvider,
        private val themeApi: ThemeApi,
        private val editPostApi: EditPostApi
) {

    fun getTheme(url: String, withHtml: Boolean, hatOpen: Boolean, pollOpen: Boolean): Observable<ThemePage> = Observable
            .fromCallable {
                themeApi.getTheme(url, hatOpen, pollOpen)
            }
            .map {
                TempHelper.transform(it, withHtml)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendPost(form: EditPostForm): Observable<ThemePage> = Observable
            .fromCallable { editPostApi.sendPost(form) }
            .map {
                TempHelper.transform(it, true)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun reportPost(themeId: Int, postId: Int, message: String): Observable<Boolean> = Observable
            .fromCallable { themeApi.reportPost(themeId, postId, message) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deletePost(postId: Int): Observable<Boolean> = Observable
            .fromCallable { themeApi.deletePost(postId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun votePost(postId: Int, type: Boolean): Observable<String> = Observable
            .fromCallable { themeApi.votePost(postId, type) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}
