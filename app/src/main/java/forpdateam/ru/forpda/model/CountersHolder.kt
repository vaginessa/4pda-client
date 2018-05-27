package forpdateam.ru.forpda.model

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.common.MessageCounters
import io.reactivex.Observable

class CountersHolder(
        private val schedulers: SchedulersProvider
) {
    private val relay = BehaviorRelay.createDefault(MessageCounters())

    fun observe(): Observable<MessageCounters> = relay
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui());

    fun get(): MessageCounters = relay.value

    fun set(value: MessageCounters) = relay.accept(value)
}