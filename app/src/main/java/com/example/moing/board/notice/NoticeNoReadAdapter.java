package com.example.moing.board.notice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moing.R;

import java.util.List;

public class NoticeNoReadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> noReadList;
    private Context context;

    public NoticeNoReadAdapter(List<String> noReadList, Context context) {
        this.noReadList = noReadList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_item_notice_noread, parent, false);
        return new NoticeNoReadAdapter.NoticeNoReadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // BoardVoteInfoResponse.voteData voteData = noReadList.get(position);
        String item_name = noReadList.get(position);
        NoticeNoReadAdapter.NoticeNoReadViewHolder vh = ( NoticeNoReadAdapter.NoticeNoReadViewHolder) holder;
        vh.name.setText(item_name);
    }

    @Override
    public int getItemCount() {
        return noReadList.size();
    }

    public class NoticeNoReadViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public NoticeNoReadViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_recycle_name);
        }
    }
}

