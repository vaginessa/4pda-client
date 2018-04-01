package forpdateam.ru.forpda.model.repository.topics

import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.topcis.TopicsApi
import io.reactivex.Observable

/**
 * Created by radiationx on 03.01.18.
 */

class TopicsRepository(
        private val schedulers: SchedulersProvider,
        private val topicsApi: TopicsApi
) {

    fun getTopics(id: Int, st: Int): Observable<TopicsData> = Observable
            .fromCallable { topicsApi.getTopics(id, st) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
