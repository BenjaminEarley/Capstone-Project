package com.benjaminearley.mysubs.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;

import com.benjaminearley.mysubs.R;
import com.benjaminearley.mysubs.data.MySubsContract;
import com.benjaminearley.mysubs.model.Child;
import com.benjaminearley.mysubs.model.Data_;
import com.benjaminearley.mysubs.model.Listing;
import com.benjaminearley.mysubs.net.RedditService;
import com.benjaminearley.mysubs.net.ServiceGenerator;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MySubsSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String ACTION_DATA_UPDATED =
            "com.benjaminearley.mysubs.ACTION_DATA_UPDATED";
    // Interval at which to sync with reddit, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    public static final int ADAPTER_SYNCING = 0;
    public static final int ADAPTER_SYNCED = 1;
    static final int COLUMN_TITLE = 1;
    static final int COLUMN_URL = 2;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int SUBREDDIT_LOADER = 0;
    private static final String[] SUBREDDIT_COLUMNS = {
            MySubsContract.SubredditEntry.TABLE_NAME + "." + MySubsContract.SubredditEntry._ID,
            MySubsContract.SubredditEntry.COLUMN_TITLE,
            MySubsContract.SubredditEntry.COLUMN_URL
    };

    public MySubsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MySubsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    static private void setLocationStatus(Context c, @SyncStatus int locationStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_sync_adapter_status_key), locationStatus);
        spe.commit();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        setLocationStatus(getContext(), ADAPTER_SYNCING);

        Uri storiesUri = MySubsContract.StoryEntry.buildStory();
        Uri subredditUri = MySubsContract.SubredditEntry.buildSubreddit();

        Cursor subreddits = getContext().getContentResolver().query(subredditUri, SUBREDDIT_COLUMNS, null, null, null);

        ArrayList<Listing> allStories = new ArrayList<>();

        if (subreddits != null) {
            while (subreddits.moveToNext()) {

                String subreddit = subreddits.getString(COLUMN_URL).substring(1);

                RedditService taskService = ServiceGenerator.createService(RedditService.class);
                Call<Listing> call = taskService.getSubredditHotListing(subreddit);
                try {
                    Listing stories = call
                            .execute()
                            .body();
                    allStories.add(stories);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            subreddits.close();

            ArrayList<Data_> storyList = new ArrayList<>();

            for (Listing stories : allStories) {

                List<Child> children = stories.getData().getChildren();

                int i = 0;

                for (Child child : children) {

                    Data_ story = child.getData();

                    if (story != null) {
                        story.setPosition(i);
                        storyList.add(story);
                        i++;
                    }
                }

            }

            Uri storyUri = MySubsContract.StoryEntry.buildStory();

            ContentValues[] storyValues = new ContentValues[storyList.size()];

            for (int i = 0; i < storyValues.length; i++) {
                storyValues[i] = new ContentValues();
                storyValues[i].put(MySubsContract.StoryEntry.COLUMN_AUTHOR, storyList.get(i).getAuthor());
                storyValues[i].put(MySubsContract.StoryEntry.COLUMN_PERMALINK, storyList.get(i).getPermalink());
                storyValues[i].put(MySubsContract.StoryEntry.COLUMN_SCORE, storyList.get(i).getScore());
                storyValues[i].put(MySubsContract.StoryEntry.COLUMN_SUBREDDIT, storyList.get(i).getSubreddit());
                storyValues[i].put(MySubsContract.StoryEntry.COLUMN_THUMBNAIL, storyList.get(i).getThumbnail());
                storyValues[i].put(MySubsContract.StoryEntry.COLUMN_TITLE, storyList.get(i).getTitle());
                storyValues[i].put(MySubsContract.StoryEntry.COLUMN_ID, storyList.get(i).getId());
                storyValues[i].put(MySubsContract.StoryEntry.COLUMN_UNIX_TIMESTAMP, storyList.get(i).getCreated_utc());
                storyValues[i].put(MySubsContract.StoryEntry.COLUMN_POSITION, storyList.get(i).getPosition());
            }

            getContext().getContentResolver().delete(storiesUri, null, null);
            getContext().getContentResolver().bulkInsert(storyUri, storyValues);

            updateWidgets();
            setLocationStatus(getContext(), ADAPTER_SYNCED);
        }



    }

    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ADAPTER_SYNCING, ADAPTER_SYNCED})
    public @interface SyncStatus {
    }
}
