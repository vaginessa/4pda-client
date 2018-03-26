package forpdateam.ru.forpda.model.repository.auth

import forpdateam.ru.forpda.model.data.remote.api.auth.Auth
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Observable

/**
 * Created by radiationx on 02.01.18.
 */

class AuthRepository(
        private val schedulers: SchedulersProvider,
        private val authApi: Auth
) {

    fun loadForm(): Observable<AuthForm> = Observable
            .fromCallable { authApi.form }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signIn(authForm: AuthForm): Observable<Boolean> = Observable
            .fromCallable { authApi.login(authForm) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


}
