package forpdateam.ru.forpda.presentation.forumrules

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
class ForumRulesPresenter(
        private val forumRepository: ForumRepository
) : BasePresenter<ForumRulesView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadData()
    }

    private fun loadData() {
        forumRepository
                .getRules(true)
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
