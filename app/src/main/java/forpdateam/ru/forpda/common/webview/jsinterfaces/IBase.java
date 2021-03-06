package forpdateam.ru.forpda.common.webview.jsinterfaces;

import android.webkit.JavascriptInterface;

/**
 * Created by radiationx on 28.05.17.
 */

public interface IBase {
    String JS_BASE_INTERFACE = "IBase";

    @JavascriptInterface
    void playClickEffect();

    //Событие DOMContentLoaded
    @JavascriptInterface
    void domContentLoaded();

    //Событие load в js
    @JavascriptInterface
    void onPageLoaded();
}
