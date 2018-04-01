package forpdateam.ru.forpda.presentation.topics

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData

/**
 * Created by radiationx on 03.01.18.
 */

interface TopicsView : IBaseView {
    fun showTopics(data: TopicsData)
    fun showItemDialogMenu(item: TopicItem)
}
