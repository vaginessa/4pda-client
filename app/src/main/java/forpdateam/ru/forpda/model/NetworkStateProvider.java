package forpdateam.ru.forpda.model;


import io.reactivex.Observable;

/**
 * Created by radiationx on 10.02.18.
 */

public interface NetworkStateProvider {

    Observable<Boolean> observeState();

    boolean getState();

    void setState(boolean state);
}
