package forpdateam.ru.forpda.presentation.mentions

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.mentions.MentionsRepository
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.IRouter
import forpdateam.ru.forpda.presentation.Screen
import java.util.regex.Pattern

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class MentionsPresenter(
        private val mentionsRepository: MentionsRepository,
        private val favoritesRepository: FavoritesRepository,
        private val router: IRouter,
        private val linkHandler: ILinkHandler
) : BasePresenter<MentionsView>() {

    var currentSt: Int = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getMentions()
    }

    fun getMentions() {
        mentionsRepository
                .getMentions(currentSt)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showMentions(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun addTopicToFavorite(topicId: Int, subType: String) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_ADD, -1, topicId, subType)
                .subscribe({
                    viewState.onAddToFavorite(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun onItemClick(item: MentionItem) {
        linkHandler.handle(item.link, router, mapOf(
                Screen.ARG_TITLE to item.title
        ))
    }

    fun onItemLongClick(item: MentionItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: MentionItem) {
        Utils.copyToClipBoard(item.link)
    }

    fun addToFavorites(item: MentionItem) {
        var id = 0
        val matcher = Pattern.compile("showtopic=(\\d+)").matcher(item.link)
        if (matcher.find()) {
            id = Integer.parseInt(matcher.group(1))
        }
        viewState.showAddFavoritesDialog(id)
    }
}
