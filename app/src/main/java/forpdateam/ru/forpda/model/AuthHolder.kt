package forpdateam.ru.forpda.model

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.common.AuthData
import io.reactivex.Observable

class AuthHolder {
    private val relay = BehaviorRelay.create<AuthData>()

    fun observe(): Observable<AuthData> = relay

    fun get(): AuthData = relay.value

    fun set(value: AuthData) = relay.accept(value)
}