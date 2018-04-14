package forpdateam.ru.forpda.presentation.forum

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree

/**
 * Created by radiationx on 03.01.18.
 */

interface ForumView : IBaseView {
    fun showForums(forumRoot: ForumItemTree)
    fun onMarkRead()
    fun onMarkAllRead()
    fun onAddToFavorite(result: Boolean)
}
