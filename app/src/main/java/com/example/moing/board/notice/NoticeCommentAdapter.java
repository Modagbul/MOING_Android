package com.example.moing.board.notice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moing.R;
import com.example.moing.board.vote.VoteCommentAdapter;
import com.example.moing.response.NoticeCommentListResponse;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NoticeCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "NoticeCommentAdapter";
    private List<NoticeCommentListResponse.NoticeComment> commentList;
    private Context context;
    private Long userId;

    // 버튼 클릭 리스너
    private VoteCommentAdapter.OnCommentButtonClickListener btnClickListener;
    public interface OnCommentButtonClickListener {
        void onCommentButtonClick(int position);
    }
    public void setOnCommentButtonClickListener(VoteCommentAdapter.OnCommentButtonClickListener listener) {
        this.btnClickListener = listener;
    }

    public NoticeCommentAdapter(List<NoticeCommentListResponse.NoticeComment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_item_notice_comment, parent, false);
        return new NoticeCommentAdapter.NoticeCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NoticeCommentListResponse.NoticeComment noticeComment = commentList.get(position);
        NoticeCommentViewHolder vh = (NoticeCommentViewHolder) holder;

        // 예정 : Profile 매칭
        vh.nickname.setText(noticeComment.getNickName());
        vh.content.setText(noticeComment.getContent());
        Glide.with(context).load(noticeComment.getUserImageUrl()).into(vh.profile);

        // 마지막 선 안보이게 처리
        if (position == commentList.size()-1) {
            vh.line.setVisibility(View.INVISIBLE);
        } else {
            vh.line.setVisibility(View.VISIBLE);
        }

        // 삭제 기능 처리
        /** 투표 댓글작성자가 나인 경우 삭제 버튼이 보이게 **/
        if(noticeComment.getUserId().equals(userId)) {
            Log.d(TAG, "userId : " + "사실");
            vh.delete.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "userId : " + "거짓");
            vh.delete.setVisibility(View.INVISIBLE);
        }
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    private class NoticeCommentViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profile;
        TextView nickname, content;
        View line;
        Button delete;

        public NoticeCommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.iv_profile);
            nickname = itemView.findViewById(R.id.tv_nickname);
            content = itemView.findViewById(R.id.tv_content);
            line = itemView.findViewById(R.id.view_board_line);
            delete = itemView.findViewById(R.id.btn_delete);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(btnClickListener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            btnClickListener.onCommentButtonClick(position);
                        }
                    }
                }
            });
        }
    }
}