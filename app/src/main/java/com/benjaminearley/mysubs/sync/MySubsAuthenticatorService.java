package com.benjaminearley.mysubs.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MySubsAuthenticatorService extends Service {

    private MySubsAuthenticator mAuthenticator;

    @Override
    public void onCreate() {

        mAuthenticator = new MySubsAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
