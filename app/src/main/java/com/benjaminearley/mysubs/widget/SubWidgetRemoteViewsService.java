package com.benjaminearley.mysubs.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.text.format.DateUtils;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.benjaminearley.mysubs.R;
import com.benjaminearley.mysubs.data.MySubsContract;

import java.util.concurrent.TimeUnit;

public class SubWidgetRemoteViewsService extends RemoteViewsService {
    static final int INDEX_STORY_ID = 0;
    static final int COLUMN_SUBREDDIT = 1;
    static final int COLUMN_AUTHOR = 2;
    static final int COLUMN_PERMALINK = 3;
    static final int COLUMN_SCORE = 4;
    static final int COLUMN_THUMBNAIL = 5;
    static final int COLUMN_UNIX_TIMESTAMP = 6;
    static final int COLUMN_ID = 7;
    static final int COLUMN_TITLE = 8;
    private static final String[] STORY_COLUMNS = {
            MySubsContract.StoryEntry.TABLE_NAME + "." + MySubsContract.StoryEntry._ID,
            MySubsContract.StoryEntry.COLUMN_SUBREDDIT,
            MySubsContract.StoryEntry.COLUMN_AUTHOR,
            MySubsContract.StoryEntry.COLUMN_PERMALINK,
            MySubsContract.StoryEntry.COLUMN_SCORE,
            MySubsContract.StoryEntry.COLUMN_THUMBNAIL,
            MySubsContract.StoryEntry.COLUMN_UNIX_TIMESTAMP,
            MySubsContract.StoryEntry.COLUMN_ID,
            MySubsContract.StoryEntry.COLUMN_TITLE
    };
    public final String LOG_TAG = SubWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri storiesUri = MySubsContract.StoryEntry.buildStory();
                data = getContentResolver().query(storiesUri,
                        STORY_COLUMNS,
                        null,
                        null,
                        MySubsContract.StoryEntry.COLUMN_POSITION + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                final String title = data.getString(COLUMN_TITLE).trim();
                final String score = String.valueOf(data.getInt(COLUMN_SCORE));
                final String link = data.getString(COLUMN_PERMALINK);
                final String thumbnail = data.getString(COLUMN_THUMBNAIL);
                final String author = data.getString(COLUMN_AUTHOR);
                final String subreddit = data.getString(COLUMN_SUBREDDIT);
                final String timePassedString = (String) DateUtils.getRelativeTimeSpanString(TimeUnit.SECONDS.toMillis(data.getLong(COLUMN_UNIX_TIMESTAMP)), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);


                views.setTextViewText(R.id.score, score);
                views.setTextViewText(R.id.author, author);
                views.setTextViewText(R.id.subreddit, subreddit);
                views.setTextViewText(R.id.title, title);
                views.setTextViewText(R.id.time, timePassedString);

                final Intent fillInIntent = new Intent();
                Uri storiesUri = MySubsContract.StoryEntry.buildStoryUri(data.getLong(COLUMN_ID));
                fillInIntent.setData(storiesUri);
                views.setOnClickFillInIntent(R.id.container, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_STORY_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
