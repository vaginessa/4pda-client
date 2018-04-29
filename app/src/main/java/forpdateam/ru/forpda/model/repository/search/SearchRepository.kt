package forpdateam.ru.forpda.model.repository.search

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.apirx.ForumUsersCache
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.api.search.SearchApi
import forpdateam.ru.forpda.model.repository.temp.TempHelper
import io.reactivex.Observable
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 01.01.18.
 */

class SearchRepository(
        private val schedulers: SchedulersProvider,
        private val searchApi: SearchApi
) {

    fun getSearch(settings: SearchSettings): Observable<SearchResult> = Observable
            .fromCallable { searchApi.getSearch(settings) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
