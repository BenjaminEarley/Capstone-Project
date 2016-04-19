package com.benjaminearley.mysubs.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MySubsContentProvider extends ContentProvider {

    static final int STORY = 100;
    static final int SUBREDDIT = 200;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MySubsDbHelper mOpenHelper;

    public MySubsContentProvider() {
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MySubsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MySubsContract.PATH_STORY, STORY);
        matcher.addURI(authority, MySubsContract.PATH_SUBREDDIT, SUBREDDIT);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MySubsDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case STORY:
                rowsDeleted = db.delete(
                        MySubsContract.StoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUBREDDIT:
                rowsDeleted = db.delete(
                        MySubsContract.SubredditEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case STORY:
                return MySubsContract.StoryEntry.CONTENT_TYPE;
            case SUBREDDIT:
                return MySubsContract.SubredditEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case STORY: {
                long _id = db.insert(MySubsContract.StoryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MySubsContract.StoryEntry.buildStoryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SUBREDDIT: {
                long _id = db.insert(MySubsContract.SubredditEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MySubsContract.SubredditEntry.buildSubredditUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case STORY: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MySubsContract.StoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case SUBREDDIT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MySubsContract.SubredditEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case STORY:
                rowsUpdated = db.update(MySubsContract.StoryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case SUBREDDIT:
                rowsUpdated = db.update(MySubsContract.SubredditEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STORY:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MySubsContract.StoryEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
