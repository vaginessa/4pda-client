package forpdateam.ru.forpda.model.repository.reputation

import forpdateam.ru.forpda.api.reputation.Reputation
import forpdateam.ru.forpda.api.reputation.models.RepData
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Observable

/**
 * Created by radiationx on 03.01.18.
 */

class ReputationRepository(
        private val schedulers: SchedulersProvider,
        private val reputationApi: Reputation
) {

    fun loadReputation(repData: RepData): Observable<RepData> = Observable
            .fromCallable { reputationApi.getReputation(repData) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun changeReputation(postId: Int, userId: Int, type: Boolean, message: String): Observable<Boolean> = Observable
            .fromCallable { reputationApi.editReputation(postId, userId, type, message) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
