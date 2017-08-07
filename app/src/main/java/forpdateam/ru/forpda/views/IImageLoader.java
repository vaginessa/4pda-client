package forpdateam.ru.forpda.views;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.views.widgets.AvatarPlaceholder;
import forpdateam.ru.forpda.views.widgets.AvatarView;

public interface IImageLoader {
    void loadImage(@NonNull AvatarView avatarView, @NonNull AvatarPlaceholder avatarPlaceholder, String avatarUrl);

    void loadImage(@NonNull AvatarView avatarView, String avatarUrl, String name);

    void loadImage(@NonNull AvatarView avatarView, String avatarUrl, String name, int textSizePercentage);
}

