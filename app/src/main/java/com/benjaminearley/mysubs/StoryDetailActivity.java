package com.benjaminearley.mysubs;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.RelativeLayout;

public class StoryDetailActivity extends AppCompatActivity {

    public static final String ARG_LIST_POSITION = "list_position";
    public static final String ARG_NO_ANIMATION = "animation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.toolbar_relative_layout);

        AppBarLayout.LayoutParams toolbarParams =
                (AppBarLayout.LayoutParams) relativeLayout.getLayoutParams();
        toolbarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(StoryDetailFragment.ARG_ITEM_TITLE,
                    getIntent().getStringExtra(StoryDetailFragment.ARG_ITEM_TITLE));
            arguments.putString(StoryDetailFragment.ARG_ITEM_LINK,
                    getIntent().getStringExtra(StoryDetailFragment.ARG_ITEM_LINK));
            arguments.putParcelable(StoryDetailFragment.DETAIL_URI, getIntent().getData());
            StoryDetailFragment fragment = new StoryDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.story_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //

            Intent intent = new Intent(this, StoryListActivity.class);
            intent.putExtra(ARG_LIST_POSITION, getIntent().getParcelableExtra(ARG_LIST_POSITION));
            intent.putExtra(ARG_NO_ANIMATION, true);

            NavUtils.navigateUpTo(this, intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
