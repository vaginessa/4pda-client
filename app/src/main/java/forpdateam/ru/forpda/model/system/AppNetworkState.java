package forpdateam.ru.forpda.model.system;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.jakewharton.rxrelay2.BehaviorRelay;

import forpdateam.ru.forpda.model.NetworkStateProvider;
import io.reactivex.Observable;

/**
 * Created by radiationx on 10.02.18.
 */

public class AppNetworkState implements NetworkStateProvider {

    private Context context;
    private BehaviorRelay<Boolean> stateRelay;

    public AppNetworkState(Context context) {
        this.context = context;
        stateRelay = BehaviorRelay.createDefault(getLocalState());
    }

    @Override
    public Observable<Boolean> observeState() {
        return stateRelay;
    }

    @Override
    public boolean getState() {
        boolean result = getLocalState();
        if (result != stateRelay.getValue()) {
            stateRelay.accept(result);
        }
        return stateRelay.getValue();
    }

    @Override
    public void setState(boolean state) {
        stateRelay.accept(state);
    }

    private boolean getLocalState() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
