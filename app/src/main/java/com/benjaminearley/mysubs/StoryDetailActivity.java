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
    public static final String ARG_ITEM_TITLE = "item_title";

    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.toolbar_relative_layout);

        AppBarLayout.LayoutParams toolbarParams =
                (AppBarLayout.LayoutParams) relativeLayout.getLayoutParams();
        toolbarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

        if (toolbar != null) {
            toolbar.setTitle(title);
        }

        if (savedInstanceState == null) {

            title = getIntent().getStringExtra(ARG_ITEM_TITLE);

            Bundle arguments = new Bundle();
            arguments.putString(StoryDetailFragment.ARG_ITEM_LINK,
                    getIntent().getStringExtra(StoryDetailFragment.ARG_ITEM_LINK));
            arguments.putParcelable(StoryDetailFragment.DETAIL_URI, getIntent().getData());
            StoryDetailFragment fragment = new StoryDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.story_detail_container, fragment)
                    .commit();
        } else {
            title = savedInstanceState.getString(ARG_ITEM_TITLE);
        }

        if (toolbar != null) {
            toolbar.setTitle(title);
        }

        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(ARG_ITEM_TITLE, title);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            Intent intent = new Intent(this, StoryListActivity.class);
            intent.putExtra(ARG_LIST_POSITION, getIntent().getParcelableExtra(ARG_LIST_POSITION));
            intent.putExtra(ARG_NO_ANIMATION, true);

            NavUtils.navigateUpTo(this, intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
