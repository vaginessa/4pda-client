package forpdateam.ru.forpda.common;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.presentation.ILinkHandler;
import forpdateam.ru.forpda.presentation.ISystemLinkHandler;
import forpdateam.ru.forpda.presentation.TabRouter;

/**
 * Created by radiationx on 04.08.16.
 */
public class IntentHandler {

    public static boolean handle(String url) {
        ILinkHandler linkHandler = App.get().Di().getLinkHandler();
        TabRouter router = App.get().Di().getRouter();
        return linkHandler.handle(url, router);
    }


    public static void handleDownload(String url) {
        ISystemLinkHandler linkHandler = App.get().Di().getSystemLinkHandler();
        linkHandler.handleDownload(url, null);
    }

}
