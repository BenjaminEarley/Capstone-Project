package com.benjaminearley.mysubs;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class storyRecyclerViewAdapter
        extends RecyclerView.Adapter<storyRecyclerViewAdapter.ViewHolder> {

    static final int COLUMN_PERMALINK = 3;
    static final int COLUMN_THUMBNAIL = 5;
    static final int COLUMN_ID = 7;
    static final int COLUMN_TITLE = 8;

    private final boolean mTwoPane;
    private final StoryListActivity activity;
    private Cursor mCursor;
    private RecyclerView recyclerView;

    public storyRecyclerViewAdapter(RecyclerView recyclerView, boolean mTwoPane, StoryListActivity activity) {
        this.mTwoPane = mTwoPane;
        this.activity = activity;
        this.recyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.story_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        final String title = mCursor.getString(COLUMN_TITLE);
        final String link = mCursor.getString(COLUMN_PERMALINK);
        final String thumbnail = mCursor.getString(COLUMN_THUMBNAIL);

        holder.identification = mCursor.getString(COLUMN_ID);

        holder.mContentView.setText(title);

        Glide.with(activity)
                .load(thumbnail)
                .centerCrop()
                .into(holder.imageView);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(StoryDetailFragment.ARG_ITEM_TITLE, title);
                    arguments.putString(StoryDetailFragment.ARG_ITEM_LINK, link);
                    StoryDetailFragment fragment = new StoryDetailFragment();
                    fragment.setArguments(arguments);
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.story_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, StoryDetailActivity.class);
                    intent.putExtra(StoryDetailFragment.ARG_ITEM_TITLE, title);
                    intent.putExtra(StoryDetailFragment.ARG_ITEM_LINK, link);
                    intent.putExtra(StoryDetailActivity.ARG_LIST_POSITION, recyclerView.getLayoutManager().onSaveInstanceState());

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public final ImageView imageView;
        public String identification;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.title);
            imageView = (ImageView) view.findViewById(R.id.image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
