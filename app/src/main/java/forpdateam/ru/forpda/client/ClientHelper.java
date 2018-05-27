package forpdateam.ru.forpda.client;

import android.util.Log;

import java.util.Observer;

import forpdateam.ru.forpda.common.simple.SimpleObservable;

/**
 * Created by radiationx on 26.03.17.
 */

public class ClientHelper {
    private final static String LOG_TAG = ClientHelper.class.getSimpleName();
    private static ClientHelper clientHelper = null;
    private SimpleObservable countsObservables = new SimpleObservable();
    private static int qmsCount = 0, mentionsCount = 0, favoritesCount = 0;

    public static ClientHelper get() {
        if (clientHelper == null) clientHelper = new ClientHelper();
        return clientHelper;
    }

    public void addCountsObserver(Observer observer) {
        countsObservables.addObserver(observer);
    }

    public void removeCountsObserver(Observer observer) {
        countsObservables.deleteObserver(observer);
    }

    public void notifyCountsChanged() {
        countsObservables.notifyObservers();
    }

    public static int getAllCounts() {
        return qmsCount + favoritesCount + mentionsCount;
    }

    public static int getQmsCount() {
        return qmsCount;
    }

    public static int getFavoritesCount() {
        return favoritesCount;
    }

    public static int getMentionsCount() {
        return mentionsCount;
    }

    public static void setQmsCount(int qmsCount) {
        ClientHelper.qmsCount = qmsCount;
    }

    public static void setFavoritesCount(int favoritesCount) {
        ClientHelper.favoritesCount = favoritesCount;
    }

    public static void setMentionsCount(int mentionsCount) {
        ClientHelper.mentionsCount = mentionsCount;
    }
}
