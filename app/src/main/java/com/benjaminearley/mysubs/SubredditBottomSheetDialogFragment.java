package com.benjaminearley.mysubs;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ListViewCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;

import com.benjaminearley.mysubs.data.MySubsContract;

public class SubredditBottomSheetDialogFragment extends BottomSheetDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SUBREDDIT_LOADER = 0;

    private static final String[] SUBREDDIT_COLUMNS = {
            MySubsContract.SubredditEntry.TABLE_NAME + "." + MySubsContract.SubredditEntry._ID,
            MySubsContract.SubredditEntry.COLUMN_TITLE,
            MySubsContract.SubredditEntry.COLUMN_URL
    };

    SimpleCursorAdapter adapter;
    ImageButton addButton;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_bottom_sheet, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        ListViewCompat listView = (ListViewCompat) contentView.findViewById(R.id.subreddit_list);

        addButton = (ImageButton) contentView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues subredditValues = new ContentValues();

                subredditValues.put(MySubsContract.SubredditEntry.COLUMN_TITLE, "test");
                subredditValues.put(MySubsContract.SubredditEntry.COLUMN_URL, "foo.com");

                getContext().getContentResolver().insert(MySubsContract.SubredditEntry.CONTENT_URI, subredditValues);
            }
        });

        String[] from = new String[]{
                MySubsContract.SubredditEntry.COLUMN_TITLE
        };


        int[] to = new int[]{android.R.id.text1};

        adapter = new SimpleCursorAdapter(contentView.getContext(), android.R.layout.simple_list_item_1, null, from, to, SimpleCursorAdapter.NO_SELECTION);

        listView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // We hold for transition here just in-case the activity
        // needs to be re-created. In a standard return transition,
        // this doesn't actually make a difference.
        getLoaderManager().initLoader(SUBREDDIT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri subredditUri = MySubsContract.SubredditEntry.buildSubreddit();

        return new CursorLoader(getActivity(),
                subredditUri,
                SUBREDDIT_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
