package forpdateam.ru.forpda.presentation.forumrules

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.forum.ForumRepository

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class ForumRulesPresenter(
        private val forumRepository: ForumRepository,
        private val forumRulesTemplate: ForumRulesTemplate
) : BasePresenter<ForumRulesView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadData()
    }

    private fun loadData() {
        forumRepository
                .getRules()
                .map { forumRulesTemplate.mapEntity(it) }
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
