package com.benjaminearley.mysubs.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.benjaminearley.mysubs.data.MySubsContract.StoryEntry;
import com.benjaminearley.mysubs.data.MySubsContract.SubredditEntry;

public class MySubsDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;

    public MySubsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_SUBREDDIT_TABLE = "CREATE TABLE " + SubredditEntry.TABLE_NAME + " (" +
                SubredditEntry._ID + " INTEGER PRIMARY KEY," +
                SubredditEntry.COLUMN_OVER_18 + " INTEGER UNIQUE NOT NULL, " +
                SubredditEntry.COLUMN_PUBLIC_TRAFFIC + " TEXT NOT NULL, " +
                SubredditEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                SubredditEntry.COLUMN_URL + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_STORY_TABLE = "CREATE TABLE " + StoryEntry.TABLE_NAME + " (" +
                StoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StoryEntry.COLUMN_SUBREDDIT_KEY + " INTEGER NOT NULL, " +
                StoryEntry.COLUMN_SUBREDDIT + " TEXT NOT NULL" +
                StoryEntry.COLUMN_AUTHOR + " TEXT NOT NULL" +
                StoryEntry.COLUMN_OVER_18 + " INTEGER NOT NULL" +
                StoryEntry.COLUMN_PERMALINK + " TEXT NOT NULL" +
                StoryEntry.COLUMN_SCORE + " INTEGER NOT NULL" +
                StoryEntry.COLUMN_THUMBNAIL + " TEXT NOT NULL" +
                StoryEntry.COLUMN_UNIX_TIMESTAMP + " INTEGER NOT NULL" +
                StoryEntry.COLUMN_TITLE + " TEXT NOT NULL" +

                " FOREIGN KEY (" + StoryEntry.COLUMN_SUBREDDIT_KEY + ") REFERENCES " +
                SubredditEntry.TABLE_NAME + " (" + SubredditEntry._ID +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_SUBREDDIT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SubredditEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StoryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
