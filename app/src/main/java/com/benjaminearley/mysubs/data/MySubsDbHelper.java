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
                SubredditEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                SubredditEntry.COLUMN_URL + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_STORY_TABLE = "CREATE TABLE " + StoryEntry.TABLE_NAME + " (" +
                StoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StoryEntry.COLUMN_SUBREDDIT + " TEXT NULLABLE," +
                StoryEntry.COLUMN_AUTHOR + " TEXT NULLABLE," +
                StoryEntry.COLUMN_OVER_18 + " INTEGER NULLABLE," +
                StoryEntry.COLUMN_PERMALINK + " TEXT NULLABLE," +
                StoryEntry.COLUMN_SCORE + " INTEGER NULLABLE," +
                StoryEntry.COLUMN_THUMBNAIL + " TEXT NULLABLE," +
                StoryEntry.COLUMN_UNIX_TIMESTAMP + " NULLABLE," +
                StoryEntry.COLUMN_TITLE + " TEXT NULLABLE);";

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
