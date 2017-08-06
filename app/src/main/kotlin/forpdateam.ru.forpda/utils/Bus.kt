package forpdateam.ru.forpda.utils

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by isanechek on 7/10/17.
 */

data class BackgroundProgress(val show: Boolean)
data class NetworkStatus(val status: Boolean)
data class ParentCallback(val title: String, val imgUrl: String)
data class SendData(val title: String, val img: String, val author: String, val url: String, val date: String)

object Bus {
    private val publisher = PublishSubject.create<Any>()

    fun publish(event: Any) { publisher.onNext(event) }

    fun <T> events(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}