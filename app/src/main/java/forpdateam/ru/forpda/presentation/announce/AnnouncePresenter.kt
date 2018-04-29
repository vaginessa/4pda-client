package forpdateam.ru.forpda.presentation.announce

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class AnnouncePresenter(
        private val forumRepository: ForumRepository,
        private val announceTemplate: AnnounceTemplate
) : BasePresenter<AnnounceView>() {

    var id = 0
    var forumId = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadData()
    }

    private fun loadData() {
        forumRepository
                .getAnnounce(id, forumId)
                .map { announceTemplate.mapEntity(it) }
                .doOnTerminate { viewState.setRefreshing(true) }
                //.doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showData(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

}
