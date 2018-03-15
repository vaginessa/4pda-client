package forpdateam.ru.forpda.model.repository.topics

import forpdateam.ru.forpda.api.topcis.Topics
import forpdateam.ru.forpda.api.topcis.models.TopicsData
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Observable

/**
 * Created by radiationx on 03.01.18.
 */

class TopicsRepository(
        private val schedulers: SchedulersProvider,
        private val topicsApi: Topics
) {

    fun getTopics(id: Int, st: Int): Observable<TopicsData> = Observable
            .fromCallable { topicsApi.getTopics(id, st) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
