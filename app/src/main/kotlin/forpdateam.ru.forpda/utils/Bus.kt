package forpdateam.ru.forpda.utils

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by isanechek on 7/10/17.
 */

data class BackgroundProgress(val show: Boolean)

object Bus {
    private val publisher = PublishSubject.create<Any>()

    fun publish(event: Any) { publisher.onNext(event) }

    fun <T> events(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}