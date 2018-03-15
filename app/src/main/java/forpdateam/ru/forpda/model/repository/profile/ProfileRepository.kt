package forpdateam.ru.forpda.model.repository.profile

import forpdateam.ru.forpda.api.profile.Profile
import forpdateam.ru.forpda.api.profile.models.ProfileModel
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Observable

/**
 * Created by radiationx on 02.01.18.
 */

class ProfileRepository(
        private val schedulers: SchedulersProvider,
        private val profileApi: Profile
) {

    fun loadProfile(url: String): Observable<ProfileModel> = Observable
            .fromCallable { profileApi.getProfile(url) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun saveNote(note: String): Observable<Boolean> = Observable
            .fromCallable { profileApi.saveNote(note) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
