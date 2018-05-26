package forpdateam.ru.forpda.presentation.auth

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class AuthPresenter(
        private val authRepository: AuthRepository,
        private val profileRepository: ProfileRepository,
        private val router: TabRouter,
        private val schedulers: SchedulersProvider
) : BasePresenter<AuthView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadForm()
    }

    fun loadForm() {
        authRepository
                .loadForm()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showForm(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun signIn(authForm: AuthForm) {
        authRepository
                .signIn(authForm)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showLoginResult(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun loadProfile(url: String) {
        profileRepository
                .loadProfile(url)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showProfile(it)
                    delayedExit()
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun onClickSkip() {
        router.replaceScreen(Screen.ArticleList())
    }

    private fun delayedExit(){
        Observable
                .just(false)
                .delay(2000L, TimeUnit.MILLISECONDS)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe {
                    ClientHelper.get().notifyAuthChanged(ClientHelper.AUTH_STATE_LOGIN)
                    router.replaceScreen(Screen.Favorites())
                }
                .addToDisposable()
    }
}
