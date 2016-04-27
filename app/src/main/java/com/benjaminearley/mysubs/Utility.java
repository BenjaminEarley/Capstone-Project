package com.benjaminearley.mysubs;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.benjaminearley.mysubs.sync.MySubsSyncAdapter;

public class Utility {

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    static public
    @MySubsSyncAdapter.SyncStatus
    int getSyncStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_sync_adapter_status_key), MySubsSyncAdapter.ADAPTER_SYNCED);
    }

}
