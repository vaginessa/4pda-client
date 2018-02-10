package forpdateam.ru.forpda.presentation.topics;

import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.api.reputation.models.RepItem;
import forpdateam.ru.forpda.api.topcis.models.TopicItem;
import forpdateam.ru.forpda.api.topcis.models.TopicsData;
import forpdateam.ru.forpda.common.mvp.IBaseView;

/**
 * Created by radiationx on 03.01.18.
 */

public interface TopicsView extends IBaseView {

    void showTopics(TopicsData data);

    void showItemDialogMenu(TopicItem item);

}
