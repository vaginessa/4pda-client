package forpdateam.ru.forpda.common.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import forpdateam.ru.forpda.App;

/**
 * Created by RadiationX on 13.08.2016.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";
    private ConnectivityManagerDelegate managerDelegate;
    private NetworkInfo.State mConnectionType;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent);
        if (managerDelegate == null) {
            managerDelegate = new ConnectivityManagerDelegate(context);
        }
        if (!(intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))) {
            return;
        }
        NetworkInfo.State newConnectionType = getCurrentConnectionType();
        if (newConnectionType == mConnectionType) return;

        mConnectionType = newConnectionType;
        Log.d(TAG, "Network connectivity changed, type is: " + mConnectionType);

        boolean state = mConnectionType == NetworkInfo.State.CONNECTED;
        Log.e("suka", "ntework send state " + state);
        App.get().Di().getNetworkState().setState(state);
    }

    public NetworkInfo.State getCurrentConnectionType() {

        if (!managerDelegate.activeNetworkExists() ||
                !managerDelegate.isConnected()) {
            return NetworkInfo.State.DISCONNECTED;
        }

        switch (managerDelegate.getNetworkType()) {
            case ConnectivityManager.TYPE_ETHERNET:
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_MOBILE:
                return NetworkInfo.State.CONNECTED;
            default:
                return NetworkInfo.State.UNKNOWN;
        }
    }

    private static class ConnectivityManagerDelegate {
        private final ConnectivityManager manager;

        ConnectivityManagerDelegate(Context context) {
            manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        boolean activeNetworkExists() {
            return manager.getActiveNetworkInfo() != null;
        }

        boolean isConnected() {
            return manager.getActiveNetworkInfo().isConnected();
        }

        int getNetworkType() {
            return manager.getActiveNetworkInfo().getType();
        }
    }
}
