package forpdateam.ru.forpda.ext

import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader

/**
 * Created by isanechek on 5/21/17.
 */
fun ImageView.loadImageFromNetwork(url: String) {
    ImageLoader.getInstance().displayImage(url, this)
}