package com.benjaminearley.mysubs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
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

import com.benjaminearley.mysubs.adapter.StoryRecyclerViewAdapter;
import com.benjaminearley.mysubs.data.MySubsContract;
import com.benjaminearley.mysubs.sync.MySubsSyncAdapter;
import com.benjaminearley.mysubs.util.AnalyticsTrackers;
import com.benjaminearley.mysubs.util.FABScrollBehavior;
import com.benjaminearley.mysubs.util.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.appinvite.AppInviteInvitation;

import java.util.Arrays;

public class StoryListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_INVITE = 0;
    private static final int SUBREDDIT_LOADER = 1;
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
    final Getter position = new Getter() {
        int i = -1;

        @Override
        public int get() {
            return i;
        }

        @Override
        public void set(int i) {
            this.i = i;
        }
    };
    private Uri storyUri = MySubsContract.StoryEntry.buildStory();
    private boolean mTwoPane;
    private boolean noEntryAnimation;
    private RecyclerView recyclerView;
    private StoryRecyclerViewAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;
    private View emptyView;
    private Uri uri;

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
        emptyView = findViewById(R.id.emptyView);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coord_layout);

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
        noEntryAnimation = getIntent().getBooleanExtra(StoryDetailActivity.ARG_NO_ANIMATION, false);

        if (savedInstanceState != null) {
            noEntryAnimation = true;
        } else {
            uri = getIntent().getData();
            if (uri != null) {
                Bundle arguments = new Bundle();
                arguments.putString(StoryDetailFragment.ARG_ITEM_LINK, uri.toString());
                StoryDetailFragment fragment = new StoryDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.story_detail_container, fragment)
                        .commit();
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
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                position.set(viewHolder.getAdapterPosition());

                getContentResolver().delete(storyUri, "id=?", new String[]{((StoryRecyclerViewAdapter.ViewHolder) viewHolder).identification});
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
                    if (adapter != null && adapter.getItemCount() != 0) {
                        MySubsSyncAdapter.syncImmediately(StoryListActivity.this);
                    } else {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }

        getSupportLoaderManager().initLoader(SUBREDDIT_LOADER, null, this);
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();

        @MySubsSyncAdapter.SyncStatus int status = Utility.getSyncStatus(this);

        switch (status) {
            case MySubsSyncAdapter.ADAPTER_SYNCING:
                swipeRefreshLayout.setRefreshing(true);
                break;
            case MySubsSyncAdapter.ADAPTER_SYNCED:
                swipeRefreshLayout.setRefreshing(false);
                break;
            default:
                swipeRefreshLayout.setRefreshing(false);
                break;
        }

        @MySubsSyncAdapter.SyncErrorStatus int errorStatus = Utility.getSyncErrorStatus(this);

        if (errorStatus == MySubsSyncAdapter.NETWORK_ERROR) {
            Snackbar.make(coordinatorLayout, R.string.network_error_message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.network_error_snackbar_action_button, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MySubsSyncAdapter.syncImmediately(StoryListActivity.this);
                        }
                    }).show();
        }
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        adapter = new StoryRecyclerViewAdapter(recyclerView, mTwoPane, noEntryAnimation, emptyView, this);
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
                Snackbar.make(coordinatorLayout, getString(R.string.invite_error), Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_action_invite_error, null).show();
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
                MySubsContract.StoryEntry.COLUMN_POSITION + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data, position.get());
        position.set(-1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null, -1);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sync_adapter_status_key)) && swipeRefreshLayout != null) {
            @MySubsSyncAdapter.SyncStatus int status = Utility.getSyncStatus(this);

            switch (status) {
                case MySubsSyncAdapter.ADAPTER_SYNCING:
                    swipeRefreshLayout.setRefreshing(true);
                    break;
                case MySubsSyncAdapter.ADAPTER_SYNCED:
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                default:
                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }
        } else if (key.equals(getString(R.string.pref_sync_error_adapter_status_key))) {
            @MySubsSyncAdapter.SyncErrorStatus int errorStatus = Utility.getSyncErrorStatus(this);

            if (errorStatus == MySubsSyncAdapter.NETWORK_ERROR) {
                Snackbar.make(coordinatorLayout, R.string.network_error_message, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.network_error_snackbar_action_button, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MySubsSyncAdapter.syncImmediately(StoryListActivity.this);
                            }
                        }).show();
            }
        }
    }

    interface Getter {
        int get();

        void set(int i);
    }

}
