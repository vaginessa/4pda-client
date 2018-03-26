package forpdateam.ru.forpda.presentation.auth

import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.common.mvp.IBaseView

/**
 * Created by radiationx on 02.01.18.
 */

interface AuthView : IBaseView {
    fun showForm(authForm: AuthForm)
    fun showLoginResult(success: Boolean)
    fun showProfile(profile: ProfileModel)
}
