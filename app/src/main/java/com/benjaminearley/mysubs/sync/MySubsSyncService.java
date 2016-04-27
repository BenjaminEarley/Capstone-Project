package com.benjaminearley.mysubs.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MySubsSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static MySubsSyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("MySubsSyncService", "onCreate - MySubsSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new MySubsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}
