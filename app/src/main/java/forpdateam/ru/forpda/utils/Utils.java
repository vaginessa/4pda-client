package forpdateam.ru.forpda.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;

import forpdateam.ru.forpda.App;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by isanechek on 30.07.16.
 */

public class Utils {
    public static boolean isMM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void copyToClipBoard(String s) {
        ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", s);
        clipboard.setPrimaryClip(clip);
    }
}
