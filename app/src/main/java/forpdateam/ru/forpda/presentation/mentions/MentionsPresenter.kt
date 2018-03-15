package forpdateam.ru.forpda.presentation.mentions

import android.os.Bundle

import com.arellomobile.mvp.InjectViewState

import java.util.regex.Matcher
import java.util.regex.Pattern

import forpdateam.ru.forpda.api.mentions.models.MentionItem
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.mentions.MentionsRepository
import forpdateam.ru.forpda.ui.fragments.TabFragment
import io.reactivex.disposables.Disposable

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class MentionsPresenter(
        private val mentionsRepository: MentionsRepository
) : BasePresenter<MentionsView>() {

    fun getMentions(st: Int) {
        val disposable = mentionsRepository.getMentions(st)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showMentions(it)
                }, {
                    this.handleErrorRx(it)
                })

        addToDisposable(disposable)
    }

    fun onItemClick(item: MentionItem) {
        val args = Bundle()
        args.putString(TabFragment.ARG_TITLE, item.title)
        IntentHandler.handle(item.link, args)
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
