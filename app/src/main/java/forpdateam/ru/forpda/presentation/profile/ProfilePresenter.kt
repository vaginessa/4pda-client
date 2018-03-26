package forpdateam.ru.forpda.presentation.profile

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository

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
            profileRepository
                    .loadProfile(it)
                    .doOnTerminate { viewState.setRefreshing(true) }
                    .doAfterTerminate { viewState.setRefreshing(false) }
                    .subscribe({ profileModel ->
                        currentData = profileModel
                        viewState.showProfile(profileModel)
                    }, {
                        this.handleErrorRx(it)
                    })
                    .addToDisposable()
        }
    }

    fun saveNote(note: String) {
        profileRepository
                .saveNote(note)
                .subscribe({
                    viewState.onSaveNote(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
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
