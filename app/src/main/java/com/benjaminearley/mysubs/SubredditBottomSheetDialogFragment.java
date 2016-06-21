package com.benjaminearley.mysubs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.benjaminearley.mysubs.adapter.BottomSheetAdapter;
import com.benjaminearley.mysubs.data.MySubsContract;
import com.benjaminearley.mysubs.model.Data__;
import com.benjaminearley.mysubs.model.Subreddit;
import com.benjaminearley.mysubs.net.WebService;
import com.benjaminearley.mysubs.sync.MySubsSyncAdapter;
import com.benjaminearley.mysubs.util.Utility;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubredditBottomSheetDialogFragment extends BottomSheetDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int COLUMN_TITLE = 1;
    public static final int COLUMN_URL = 2;
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
    Integer deletePosition = null;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new CustomWidthBottomSheetDialog(getActivity(), getTheme());
    }

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
            public void onClick(final String subreddit, final int position) {

                new AlertDialog.Builder(getContext())
                        .setMessage(
                                String.format(
                                        getActivity().getString(R.string.delete_subreddit_message),
                                        subreddit))

                        .setPositiveButton(getActivity().getString(R.string.delete_subreddit_positive_button_text),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri subredditUri = MySubsContract.SubredditEntry.buildSubreddit();
                                        deletePosition = position;
                                        getContext().getContentResolver().delete(subredditUri, getActivity().getString(R.string.db_title_query), new String[]{subreddit});
                                        MySubsSyncAdapter.syncImmediately(getContext());
                                    }
                                })
                        .setNegativeButton(getActivity().getString(R.string.delete_subreddit_negative_button_text), null)
                        .create()
                        .show();
            }
        });
        recyclerView.setAdapter(subredditAdapter);

        spinner = (ProgressBar) contentView.findViewById(R.id.spinner);
        subredditSearch = (EditText) contentView.findViewById(R.id.subreddit_search);

        subredditSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().replaceAll(" ", "").toLowerCase();
                if (!s.toString().equals(result)) {
                    subredditSearch.setText(result);
                    subredditSearch.setSelection(result.length());
                    // alert the user
                }
            }
        });

        addButton = (ImageButton) contentView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchedText = subredditSearch.getText().toString();

                if (searchedText.isEmpty()) {
                    return;
                }

                if (Utility.isNetworkAvailable(getContext())) {

                    showProgress(true);
                    WebService.getInstance().getRedditService().getSubredditInfo(searchedText).enqueue(new Callback<Subreddit>() {
                        @Override
                        public void onResponse(Call<Subreddit> call, Response<Subreddit> response) {

                            if (!response.isSuccessful()) {
                                showDialog(getActivity().getString(R.string.add_subreddit_unable_to_connect));
                                return;
                            }

                            if (response.raw().toString().contains(getActivity().getString(R.string.no_subreddit_by_that_name_json_check))) {
                                showDialog(getActivity().getString(R.string.add_subreddit_does_not_exist));
                                return;
                            }

                            Data__ subredditData = response.body().getData();

                            if (subredditData.isOver18()) {
                                showDialog(getActivity().getString(R.string.add_subreddit_age_restricted));
                                return;
                            }

                            data.move(-1);

                            while (data.moveToNext()) {
                                if (data.getString(COLUMN_URL).equals(subredditData.getUrl())) {
                                    showDialog(getActivity().getString(R.string.duplicate_sub));
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
                            showDialog(getActivity().getString(R.string.add_subreddit_unable_to_connect));
                        }
                    });
                } else {
                    showDialog(getActivity().getString(R.string.add_subreddit_not_internet));
                }

            }
        });


    }

    private void showDialog(String message) {
        showProgress(false);
        new AlertDialog
                .Builder(getContext())
                .setMessage(message)
                .setPositiveButton(getActivity().getString(R.string.basic_dialog_positive_button_text), null)
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
        subredditAdapter.swapCursor(data, deletePosition);
        deletePosition = null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        subredditAdapter.swapCursor(null, -1);
    }

    private void showProgress(final boolean show) {


        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        addButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                addButton.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        spinner.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                spinner.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

    public interface SimpleAdapterOnClickHandler {
        void onClick(String subreddit, int position);
    }

    static class CustomWidthBottomSheetDialog extends BottomSheetDialog {
        public CustomWidthBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
            super(context, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            int width = getContext().getResources().getDimensionPixelSize(R.dimen.bottom_sheet_width);
            getWindow().setLayout(width > 0 ? width : ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

}
