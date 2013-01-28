package me.jromero.lib.simpleandroidtunnel;

import me.jromero.lib.simpleandroidtunnel.LocalPortForwardingTask.LocalPortForwardingListener;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TunnelService extends Service {

    public final String TAG = "TunnelService";

    public static final int MSG_CONNECTING = 1;
    public static final int MSG_CONNECTED = 2;
    public static final int MSG_DISCONNECTED = 3;

    public class ServiceBinder extends Binder {
        public TunnelService getService() {
            return TunnelService.this;
        }
    }

    private final IBinder binder = new ServiceBinder();

    /**
     * Start a local port forwarding task
     * 
     * @param config
     * @param listener
     * @return The executed {@link LocalPortForwardingTask}
     */
    public LocalPortForwardingTask localPortForwarding(
            LocalPortForwardingConfig config,
            LocalPortForwardingListener listener) {

        LocalPortForwardingTask task = new LocalPortForwardingTask();
        task.setLocalPortForwardingListener(listener);
        task.execute(config);
        return task;
    }

    /**
     * When binding to the service, we return an interface to our messenger for
     * sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");
    }
}
