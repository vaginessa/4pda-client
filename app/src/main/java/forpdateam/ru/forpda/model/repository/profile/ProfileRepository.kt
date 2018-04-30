package forpdateam.ru.forpda.model.repository.profile

import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.profile.ProfileApi
import io.reactivex.Observable

/**
 * Created by radiationx on 02.01.18.
 */

class ProfileRepository(
        private val schedulers: SchedulersProvider,
        private val profileApi: ProfileApi
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
