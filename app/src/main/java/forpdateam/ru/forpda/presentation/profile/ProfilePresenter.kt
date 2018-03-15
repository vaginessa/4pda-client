package forpdateam.ru.forpda.presentation.profile

import com.arellomobile.mvp.InjectViewState

import forpdateam.ru.forpda.api.profile.models.ProfileModel
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import io.reactivex.disposables.Disposable

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class ProfilePresenter(
        private val profileRepository: ProfileRepository
) : BasePresenter<ProfileView>() {

    var profileUrl: String? = null
    private var currentData: ProfileModel? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadProfile()
    }

    private fun loadProfile() {
        profileUrl?.let {
            val disposable = profileRepository.loadProfile(it)
                    .doOnTerminate { viewState.setRefreshing(true) }
                    .doAfterTerminate { viewState.setRefreshing(false) }
                    .subscribe({ profileModel ->
                        currentData = profileModel
                        viewState.showProfile(profileModel)
                    }, {
                        this.handleErrorRx(it)
                    })
            addToDisposable(disposable)
        }
    }

    fun saveNote(note: String) {
        val disposable = profileRepository.saveNote(note)
                .subscribe({
                    viewState.onSaveNote(it)
                }, {
                    this.handleErrorRx(it)
                })
        addToDisposable(disposable)
    }

    fun copyUrl() {
        Utils.copyToClipBoard(profileUrl)
    }

    fun navigateToQms() {
        currentData?.let {
            IntentHandler.handle(it.contacts[0].url)
        }
    }
}
