package com.benjaminearley.mysubs.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MySubsContract {

    public static final String CONTENT_AUTHORITY = "com.benjaminearley.mysubs";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_SUBREDDIT = "subreddit";
    public static final String PATH_STORY = "story";

    /* Inner class that defines the table contents of the location table */
    public static final class SubredditEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBREDDIT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDIT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDIT;

        public static final String TABLE_NAME = "subreddit";

        public static final String COLUMN_URL = "url";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_OVER_18 = "over18";

        public static final String COLUMN_PUBLIC_TRAFFIC = "publicTraffic";

        public static Uri buildSubredditUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the weather table */
    public static final class StoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STORY;

        public static final String TABLE_NAME = "story";

        public static final String COLUMN_SUBREDDIT_KEY = "subreddit_id";

        public static final String COLUMN_SUBREDDIT = "subreddit";

        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_SCORE = "score";

        public static final String COLUMN_OVER_18 = "over18";

        public static final String COLUMN_THUMBNAIL = "thumbnail";

        public static final String COLUMN_PERMALINK = "permalink";

        public static final String COLUMN_UNIX_TIMESTAMP = "unixTimestamp";

        public static final String COLUMN_TITLE = "title";

        public static Uri buildStoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }
}