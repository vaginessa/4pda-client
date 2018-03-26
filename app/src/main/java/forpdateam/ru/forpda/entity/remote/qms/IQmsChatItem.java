package forpdateam.ru.forpda.entity.remote.qms;

/**
 * Created by radiationx on 03.08.16.
 */
public interface IQmsChatItem {
    boolean isMyMessage();

    int getId();

    boolean getReadStatus();

    String getTime();

    String getAvatar();

    String getContent();

    boolean isDate();

    String getDate();
}
