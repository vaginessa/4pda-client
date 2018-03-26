package forpdateam.ru.forpda.apirx;

import forpdateam.ru.forpda.apirx.apiclasses.QmsRx;

/**
 * Created by radiationx on 25.03.17.
 */

public class RxApi {
    private static RxApi INSTANCE = null;
    private static QmsRx qmsApi = null;

    public static QmsRx Qms() {
        if (qmsApi == null) qmsApi = new QmsRx();
        return qmsApi;
    }

    public static RxApi get() {
        if (INSTANCE == null) INSTANCE = new RxApi();
        return INSTANCE;
    }
}
