package forpdateam.ru.forpda.presentation.forumrules

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumRules

/**
 * Created by radiationx on 02.01.18.
 */

interface ForumRulesView : IBaseView {
    fun showData(data: ForumRules)
}
