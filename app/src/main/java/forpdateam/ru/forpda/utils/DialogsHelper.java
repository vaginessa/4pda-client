package forpdateam.ru.forpda.utils;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.WebView;

/**
 * Created by radiationx on 01.11.16.
 */

public class DialogsHelper {
    private static AlertDialogMenu<Pair<String, String>> alertDialogMenu = new AlertDialogMenu<>();
    private final static String openNewTab = "Открыть в новой вкладке";
    private final static String openBrowser = "Открыть в браузере";
    private final static String copyUrl = "Скопировать ссылку";
    private final static String openImage = "Открыть изображение";
    private final static String saveImage = "Сохранить изображение";
    private final static String copyImageUrl = "Скопировать ссылку изображения";

    public static void handleContextMenu(Context context, int type, String extra, String nodeHref) {
        Log.d("kek", "context " + type + " : " + extra + " : " + nodeHref);
        if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE)
            return;
        type = type == WebView.HitTestResult.ANCHOR_TYPE ? WebView.HitTestResult.SRC_ANCHOR_TYPE : type;
        type = type == WebView.HitTestResult.IMAGE_ANCHOR_TYPE ? WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE : type;

        int index;
        boolean anchor = false, image = false;
        switch (type) {
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                anchor = true;
                break;
            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                anchor = true;
                image = true;
                break;
            case WebView.HitTestResult.IMAGE_TYPE:
                image = true;
                break;
        }

        if (image)
            image = !extra.contains("4pda.ru/forum/style_images");
        if (!anchor && !image)
            return;

        if (anchor) {
            if (alertDialogMenu.containsIndex(openNewTab) == -1)
                alertDialogMenu.addItem(openNewTab, new AlertDialogMenu.OnClickListener<Pair<String, String>>() {
                    @Override
                    public void onClick(Pair<String, String> data) {

                        IntentHandler.handle(data.second);
                    }
                });
            if (alertDialogMenu.containsIndex(openBrowser) == -1)
                alertDialogMenu.addItem(openBrowser, null);
            if (alertDialogMenu.containsIndex(copyUrl) == -1)
                alertDialogMenu.addItem(copyUrl, data -> Utils.copyToClipBoard(data.second));
        } else {
            index = alertDialogMenu.containsIndex(openNewTab);
            if (index != -1)
                alertDialogMenu.remove(index);
            index = alertDialogMenu.containsIndex(openBrowser);
            if (index != -1)
                alertDialogMenu.remove(index);
            index = alertDialogMenu.containsIndex(copyUrl);
            if (index != -1)
                alertDialogMenu.remove(index);
        }
        if (image) {
            if (alertDialogMenu.containsIndex(openImage) == -1)
                alertDialogMenu.addItem(openImage, null);
            if (alertDialogMenu.containsIndex(saveImage) == -1)
                alertDialogMenu.addItem(saveImage, null);
            if (alertDialogMenu.containsIndex(copyImageUrl) == -1)
                alertDialogMenu.addItem(copyImageUrl, data -> Utils.copyToClipBoard(data.first));
        } else {
            index = alertDialogMenu.containsIndex(openImage);
            if (index != -1)
                alertDialogMenu.remove(index);
            index = alertDialogMenu.containsIndex(saveImage);
            if (index != -1)
                alertDialogMenu.remove(index);
            index = alertDialogMenu.containsIndex(copyImageUrl);
            if (index != -1)
                alertDialogMenu.remove(index);
        }
        new AlertDialog.Builder(context)
                .setItems(alertDialogMenu.getTitles(), (dialog, which) -> alertDialogMenu.onClick(which, new Pair<>(extra, nodeHref)))
                .show();
    }
}
