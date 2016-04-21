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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.benjaminearley.mysubs.data.MySubsContract;
import com.benjaminearley.mysubs.model.Data__;
import com.benjaminearley.mysubs.model.Subreddit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubredditBottomSheetDialogFragment extends BottomSheetDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COLUMN_TITLE = 1;
    static final int COLUMN_URL = 2;
    private static final int SUBREDDIT_LOADER = 0;
    private static final String[] SUBREDDIT_COLUMNS = {
            MySubsContract.SubredditEntry.TABLE_NAME + "." + MySubsContract.SubredditEntry._ID,
            MySubsContract.SubredditEntry.COLUMN_TITLE,
            MySubsContract.SubredditEntry.COLUMN_URL
    };
    ImageButton addButton;
    EditText subredditSearch;
    BottomSheetAdapter subredditAdapter;
    RecyclerView recyclerView;

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

        recyclerView = (RecyclerView) contentView.findViewById(R.id.subreddit_list);
        subredditAdapter = new BottomSheetAdapter(new SimpleAdapterOnClickHandler() {
            @Override
            public void onClick(String subreddit) {
                Toast.makeText(getContext(), subreddit, Toast.LENGTH_LONG).show();
            }
        });
        recyclerView.setAdapter(subredditAdapter);

        subredditSearch = (EditText) contentView.findViewById(R.id.subreddit_search);
        addButton = (ImageButton) contentView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchedText = subredditSearch.getText().toString();

                if (searchedText.isEmpty()) {
                    return;
                }
                WebService.getInstance().getRedditService().getSubredditInfo(searchedText).enqueue(new Callback<Subreddit>() {
                    @Override
                    public void onResponse(Call<Subreddit> call, Response<Subreddit> response) {

                        if (!response.isSuccessful()) {
                            return;
                        }

                        if (response.raw().toString().contains("search.json?q")) {
                            return;
                        }

                        Data__ subredditData = response.body().getData();

                        if (subredditData.isOver18() || subredditData.isPublicTraffic()) {
                            return;
                        }

//                        mCursor.move(-1);
//
//                        while (mCursor.moveToNext()) {
//                            if (mCursor.getString(COLUMN_URL).equals(subredditData.getUrl())) {
//                                return;
//                            }
//                        }

                        ContentValues subredditValues = new ContentValues();

                        subredditValues.put(MySubsContract.SubredditEntry.COLUMN_TITLE, subredditData.getTitle());
                        subredditValues.put(MySubsContract.SubredditEntry.COLUMN_URL, subredditData.getUrl());

                        getContext().getContentResolver().insert(MySubsContract.SubredditEntry.CONTENT_URI, subredditValues);

                        subredditSearch.setText("");
                    }

                    @Override
                    public void onFailure(Call<Subreddit> call, Throwable t) {

                    }
                });

            }
        });


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
                MySubsContract.SubredditEntry._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        subredditAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        subredditAdapter.swapCursor(null);
    }

    public interface SimpleAdapterOnClickHandler {
        void onClick(String subreddit);
    }

}
