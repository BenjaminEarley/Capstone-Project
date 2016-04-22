package com.benjaminearley.mysubs;

import android.app.Application;

import com.google.android.gms.analytics.Tracker;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AnalyticsTrackers.initialize(this);

        Tracker tracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);

        tracker.setAppName("MySubs");
        tracker.setAppVersion(BuildConfig.VERSION_NAME);
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);
    }
}
