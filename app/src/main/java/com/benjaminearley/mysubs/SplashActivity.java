package com.benjaminearley.mysubs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.benjaminearley.mysubs.sync.MySubsSyncAdapter;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MySubsSyncAdapter.syncImmediately(this);

        Intent intent = new Intent(this, StoryListActivity.class);
        startActivity(intent);
        finish();
    }
}