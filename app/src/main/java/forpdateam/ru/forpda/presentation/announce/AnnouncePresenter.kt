package forpdateam.ru.forpda.presentation.announce

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class AnnouncePresenter(
        private val forumRepository: ForumRepository
) : BasePresenter<AnnounceView>() {

    var id = 0
    var forumId = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadData()
    }

    private fun loadData() {
        forumRepository
                .getAnnounce(id, forumId, true)
                .doOnTerminate { viewState.setRefreshing(true) }
                //.doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({

                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

}
