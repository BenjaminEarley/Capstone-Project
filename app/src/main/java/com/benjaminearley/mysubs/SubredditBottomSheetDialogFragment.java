package com.benjaminearley.mysubs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.benjaminearley.mysubs.data.MySubsContract;
import com.benjaminearley.mysubs.model.Data__;
import com.benjaminearley.mysubs.model.Subreddit;
import com.benjaminearley.mysubs.sync.MySubsSyncAdapter;

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
    Cursor data;
    ProgressBar spinner;

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
            public void onClick(final String subreddit) {

                new AlertDialog.Builder(getContext())
                        .setMessage("Are you sure you want to delete \"" + subreddit + "\"?")
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri subredditUri = MySubsContract.SubredditEntry.buildSubreddit();
                                        getContext().getContentResolver().delete(subredditUri, "title=?", new String[]{subreddit});
                                        MySubsSyncAdapter.syncImmediately(getContext());
                                    }
                                })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            }
        });
        recyclerView.setAdapter(subredditAdapter);

        spinner = (ProgressBar) contentView.findViewById(R.id.spinner);
        subredditSearch = (EditText) contentView.findViewById(R.id.subreddit_search);
        addButton = (ImageButton) contentView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchedText = subredditSearch.getText().toString();

                if (searchedText.isEmpty()) {
                    return;
                }

                showProgress(true);

                WebService.getInstance().getRedditService().getSubredditInfo(searchedText).enqueue(new Callback<Subreddit>() {
                    @Override
                    public void onResponse(Call<Subreddit> call, Response<Subreddit> response) {

                        if (!response.isSuccessful()) {
                            showDialog("Unable to connect to Reddit. Please try again later");
                            return;
                        }

                        if (response.raw().toString().contains("search.json?q")) {
                            showDialog("This subreddit does not exist");
                            return;
                        }

                        Data__ subredditData = response.body().getData();

                        if (!subredditData.isPublicTraffic()) {
                            showDialog("This subreddit is set to private. Sorry for the inconvenience");
                            return;
                        }

                        if (subredditData.isOver18()) {
                            showDialog("This subreddit is age restricted and not allowed in this app. Sorry for the inconvenience");
                            return;
                        }

                        data.move(-1);

                        while (data.moveToNext()) {
                            if (data.getString(COLUMN_URL).equals(subredditData.getUrl())) {
                                showDialog("You have already subscribed to this sub");
                                return;
                            }
                        }

                        ContentValues subredditValues = new ContentValues();

                        subredditValues.put(MySubsContract.SubredditEntry.COLUMN_TITLE, subredditData.getTitle());
                        subredditValues.put(MySubsContract.SubredditEntry.COLUMN_URL, subredditData.getUrl());

                        getContext().getContentResolver().insert(MySubsContract.SubredditEntry.CONTENT_URI, subredditValues);

                        subredditSearch.setText("");
                        showProgress(false);
                        MySubsSyncAdapter.syncImmediately(getContext());
                    }

                    @Override
                    public void onFailure(Call<Subreddit> call, Throwable t) {
                        showDialog("Unable to connect to Reddit. Please try again later");
                    }
                });

            }
        });


    }

    private void showDialog(String message) {
        showProgress(false);
        new AlertDialog
                .Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create()
                .show();

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
        this.data = data;
        subredditAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        subredditAdapter.swapCursor(null);
    }

    private void showProgress(final boolean show) {


        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        addButton.setVisibility(show ? View.GONE : View.VISIBLE);
        addButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                addButton.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        spinner.setVisibility(show ? View.VISIBLE : View.GONE);
        spinner.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                spinner.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

    public interface SimpleAdapterOnClickHandler {
        void onClick(String subreddit);
    }

}
