package forpdateam.ru.forpda.utils;

import android.support.annotation.NonNull;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.views.widgets.AvatarPlaceholder;
import forpdateam.ru.forpda.views.widgets.AvatarView;

/**
 * Created by isanechek on 8/7/17.
 */

public class UILLoader extends ImageLoaderBase {

    public UILLoader() {
    }

    @Override
    public void loadImage(@NonNull AvatarView avatarView, @NonNull AvatarPlaceholder avatarPlaceholder, String avatarUrl) {
        ImageLoader.getInstance().displayImage(avatarUrl, avatarView);
    }
}
