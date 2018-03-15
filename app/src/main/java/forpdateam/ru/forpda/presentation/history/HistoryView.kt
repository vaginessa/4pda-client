package forpdateam.ru.forpda.presentation.history

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.app.history.HistoryItem

/**
 * Created by radiationx on 01.01.18.
 */

interface HistoryView : IBaseView {
    fun showHistory(items: List<HistoryItem>)
    fun showItemDialogMenu(item: HistoryItem)
}
