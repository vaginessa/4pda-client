package forpdateam.ru.forpda.presentation.topics

import forpdateam.ru.forpda.api.topcis.models.TopicItem
import forpdateam.ru.forpda.api.topcis.models.TopicsData
import forpdateam.ru.forpda.common.mvp.IBaseView

/**
 * Created by radiationx on 03.01.18.
 */

interface TopicsView : IBaseView {
    fun showTopics(data: TopicsData)
    fun showItemDialogMenu(item: TopicItem)
}
