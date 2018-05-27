package forpdateam.ru.forpda.model.repository.auth

import forpdateam.ru.forpda.entity.common.AuthState
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.auth.AuthApi
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 02.01.18.
 */

class AuthRepository(
        private val schedulers: SchedulersProvider,
        private val authApi: AuthApi,
        private val authHolder: AuthHolder,
        private val countersHolder: CountersHolder
) {

    fun loadForm(): Observable<AuthForm> = Observable
            .fromCallable { authApi.form }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signIn(authForm: AuthForm): Observable<Boolean> = Observable
            .fromCallable { authApi.login(authForm) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signOut(): Single<Boolean> = Single
            .fromCallable { authApi.logout() }
            .doOnSuccess {
                authHolder.set(authHolder.get().apply {
                    userId = 0
                    state = AuthState.NO_AUTH
                })
                countersHolder.set(countersHolder.get().apply {
                    mentions = 0
                    favorites = 0
                    qms = 0
                })
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
