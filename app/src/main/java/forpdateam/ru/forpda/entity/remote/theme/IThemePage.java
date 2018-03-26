package forpdateam.ru.forpda.entity.remote.theme;

import java.util.ArrayList;

import forpdateam.ru.forpda.entity.remote.theme.ThemePost;

/**
 * Created by radiationx on 04.08.16.
 */
public interface IThemePage {
    String getTitle();

    String getDesc();

    boolean isInFavorite();

    ArrayList<ThemePost> getPosts();

    String getHtml();
}
