package com.benjaminearley.mysubs.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.benjaminearley.mysubs.R;
import com.benjaminearley.mysubs.SubredditBottomSheetDialogFragment;

public class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.ViewHolder> {

    final private SubredditBottomSheetDialogFragment.SimpleAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;
    private Integer removalPosition = null;


    public BottomSheetAdapter(SubredditBottomSheetDialogFragment.SimpleAdapterOnClickHandler mClickHandler) {
        this.mClickHandler = mClickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.bottom_sheet_fragment_dialog_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mTextView.setText(mCursor.getString(SubredditBottomSheetDialogFragment.COLUMN_TITLE));
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor, Integer deletePosition) {
        mCursor = newCursor;
        if (newCursor != null) {
            if (deletePosition == null) {
                notifyItemInserted(0);
            } else if (deletePosition == -1) {
                notifyDataSetChanged();
            } else {
                notifyItemRemoved(deletePosition);
            }
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private TextView mTextView;


        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(android.R.id.title);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(mCursor.getString(SubredditBottomSheetDialogFragment.COLUMN_TITLE), adapterPosition);
            return true;
        }
    }
}
