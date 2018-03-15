package forpdateam.ru.forpda.model.repository.mentions

import forpdateam.ru.forpda.api.mentions.Mentions
import forpdateam.ru.forpda.api.mentions.models.MentionsData
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Observable

/**
 * Created by radiationx on 01.01.18.
 */

class MentionsRepository(
        private val schedulers: SchedulersProvider,
        private val mentionsApi: Mentions
) {

    fun getMentions(page: Int): Observable<MentionsData> = Observable
            .fromCallable { mentionsApi.getMentions(page) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
