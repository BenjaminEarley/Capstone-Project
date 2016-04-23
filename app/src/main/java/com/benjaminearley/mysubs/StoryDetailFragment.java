package com.benjaminearley.mysubs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StoryDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    private String title;

    public StoryDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            title = getArguments().getString(ARG_ITEM_ID);

            Activity activity = this.getActivity();
            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.detail_toolbar);
            if (toolbar != null) {
                try {
                    ((StoryDetailActivity) activity).getSupportActionBar().setTitle(title);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.story_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (title != null) {
            ((TextView) rootView.findViewById(R.id.story_detail)).setText(title);
        }

        return rootView;
    }
}
