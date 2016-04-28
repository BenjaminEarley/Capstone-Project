package com.benjaminearley.mysubs;

import android.app.Application;

import com.benjaminearley.mysubs.sync.MySubsSyncAdapter;
import com.benjaminearley.mysubs.util.AnalyticsTrackers;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        MySubsSyncAdapter.initializeSyncAdapter(this);

        AnalyticsTrackers.initialize(this);

        Tracker tracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);

        tracker.setAppName("MySubs");
        tracker.setAppVersion(BuildConfig.VERSION_NAME);
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);
    }
}
