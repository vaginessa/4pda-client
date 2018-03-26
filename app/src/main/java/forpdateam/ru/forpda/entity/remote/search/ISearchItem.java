package forpdateam.ru.forpda.entity.remote.search;

import forpdateam.ru.forpda.entity.remote.IBaseForumPost;

/**
 * Created by radiationx on 27.04.17.
 */

public interface ISearchItem extends IBaseForumPost {
    String getImageUrl();

    String getTitle();

    String getDesc();
}
