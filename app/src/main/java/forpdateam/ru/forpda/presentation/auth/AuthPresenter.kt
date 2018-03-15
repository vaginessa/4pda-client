package forpdateam.ru.forpda.presentation.auth

import com.arellomobile.mvp.InjectViewState

import forpdateam.ru.forpda.api.auth.models.AuthForm
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import io.reactivex.disposables.Disposable

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class AuthPresenter(
        private val authRepository: AuthRepository,
        private val profileRepository: ProfileRepository
) : BasePresenter<AuthView>() {

    fun loadForm() {
        val disposable = authRepository.loadForm()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showForm(it)
                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
    }

    fun signIn(authForm: AuthForm) {
        val disposable = authRepository.signIn(authForm)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showLoginResult(it)
                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
    }

    fun loadProfile(url: String) {
        val disposable = profileRepository.loadProfile(url)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showProfile(it)
                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
    }
}
