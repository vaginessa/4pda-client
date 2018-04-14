package forpdateam.ru.forpda.presentation.announce

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.forum.Announce

/**
 * Created by radiationx on 02.01.18.
 */

interface AnnounceView : IBaseView {
    fun showData(data: Announce)
}
