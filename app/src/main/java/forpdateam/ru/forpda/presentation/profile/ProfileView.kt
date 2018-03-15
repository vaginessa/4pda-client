package forpdateam.ru.forpda.presentation.profile

import forpdateam.ru.forpda.api.profile.models.ProfileModel
import forpdateam.ru.forpda.common.mvp.IBaseView

/**
 * Created by radiationx on 02.01.18.
 */

interface ProfileView : IBaseView {
    fun showProfile(profile: ProfileModel)
    fun onSaveNote(success: Boolean)
}
