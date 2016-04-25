package com.benjaminearley.mysubs;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.benjaminearley.mysubs.data.MySubsContract;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.appinvite.AppInviteInvitation;

import java.util.Arrays;

public class StoryListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_INVITE = 0;
    private static final int SUBREDDIT_LOADER = 1;
    private static final String TAG = StoryListActivity.class.getSimpleName();
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

    private Uri storyUri = MySubsContract.StoryEntry.buildStory();
    private boolean mTwoPane;
    private RecyclerView recyclerView;
    private storyRecyclerViewAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getTitle());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BottomSheetDialogFragment bottomSheetDialogFragment = new SubredditBottomSheetDialogFragment();
                    bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            });
        }

        recyclerView = (RecyclerView) findViewById(R.id.story_list);

        if (findViewById(R.id.story_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        } else {
            try {
                AppBarLayout.LayoutParams toolbarParams =
                        (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
                toolbarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

                CoordinatorLayout.LayoutParams fabParams =
                        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                fabParams.setBehavior(new FABScrollBehavior());
            } catch (NullPointerException ignored) {
            }
        }
        if (recyclerView != null) {
            setupRecyclerView(recyclerView);
        }


        if (recyclerView != null) {
            Parcelable parcelable = getIntent().getParcelableExtra(StoryDetailActivity.ARG_LIST_POSITION);
            if (parcelable != null) {
                recyclerView.getLayoutManager().onRestoreInstanceState(parcelable);
            }
        }

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                getContentResolver().delete(storyUri, "id=?", new String[]{((storyRecyclerViewAdapter.ViewHolder) viewHolder).identification});
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(recyclerView);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {

            swipeRefreshLayout.setColorSchemeResources(
                    R.color.colorPrimary
            );

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }


        getSupportLoaderManager().initLoader(SUBREDDIT_LOADER, null, this);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        adapter = new storyRecyclerViewAdapter(recyclerView, mTwoPane, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.invite:
                onInviteClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                AnalyticsTrackers
                        .getInstance()
                        .get(AnalyticsTrackers.Target.APP)
                        .send(new HitBuilders.EventBuilder()
                                .setAction("GAPPS_INVITE")
                                .setCategory("SHARING")
                                .set("Invintation Count", Arrays.toString(ids))
                                .build()
                        );
            } else {
                Snackbar.make(findViewById(R.id.coord_layout), getString(R.string.invite_error), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setGoogleAnalyticsTrackingId("UA-76358424-1")
                .setEmailHtmlContent("<html><body>" +
                        "<h1>" + getString(R.string.invite_h1) + "</h1>" +
                        "<a href=\"%%APPINVITE_LINK_PLACEHOLDER%%\">" + getString(R.string.invite_a) + "</a>" +
                        "<body></html>")
                .setEmailSubject(getString(R.string.invitation_subject))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,
                storyUri,
                STORY_COLUMNS,
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
