package com.example.moing.board.notice;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moing.R;
import com.example.moing.response.AllNoticeResponse;
import com.example.moing.s3.DownloadImageCallback;
import com.example.moing.s3.S3Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NoticeViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<AllNoticeResponse.NoticeBlock> listData;
    private Context context;

    // 클릭 아이템 리스너
    private OnItemClickListener onItemClickListener = null;

    // 클릭 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public NoticeViewAdapter(List<AllNoticeResponse.NoticeBlock> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_item_notice, parent, false);
        return new NoticeViewAdapter.NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AllNoticeResponse.NoticeBlock item = listData.get(position);
        NoticeViewHolder vh = (NoticeViewHolder) holder;

        /**S3Glide**/
        S3Utils.downloadImageFromS3(item.getUserImageUrl(), new DownloadImageCallback() {
            @Override
            public void onImageDownloaded(byte[] data) {
                if(context != null){
                    runOnUiThread(() ->  Glide.with(context)
                            .load(data)
                            .into(vh.image));
                }
            }
            @Override
            public void onImageDownloadFailed() {
                if(context != null) {
                    runOnUiThread(() -> Glide.with(context)
                            .load(item.getUserImageUrl())
                            .into(vh.image));
                }
            }
        });
        vh.image.setBackgroundResource(R.drawable.notice_profile); // 닉네임
        vh.name.setText(item.getNickName()); // 이름
        vh.crown.setBackgroundResource(R.drawable.notice_crown); // 왕관
        vh.title.setText(item.getTitle());
        vh.content.setText(item.getContent());
        vh.dot.setBackgroundResource(R.drawable.notice_dot);
        vh.count.setText(String.valueOf(item.getCommentNum()));

        boolean read = item.getRead();
        vh.dot.setVisibility(read ? View.INVISIBLE : View.VISIBLE); // 참이면 안보이게, 거짓이면 보이게
        // 읽었다면
        if(read) {
            int colorRes = R.color.secondary_grey_black_14;
            // 리소스 식별자를 사용하여 ColorStateList 생성
            ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes));
            // ConstraintLayout 배경색 설정
            vh.background.setBackgroundTintList(colorStateList);
            vh.line.setVisibility(View.VISIBLE);
        }
        else {
            int colorRes = R.color.secondary_grey_black_12;
            // 리소스 식별자를 사용하여 ColorStateList 생성
            ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes));
            // ConstraintLayout 배경색 설정
            vh.background.setBackgroundTintList(colorStateList);
            vh.line.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class NoticeViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        ImageView dot, crown, iv_msg;
        ConstraintLayout background;
        TextView name, title, content, count, line;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);

            background = itemView.findViewById(R.id.recycle_background);
            crown = itemView.findViewById(R.id.notice_iv_crown);
            image = itemView.findViewById(R.id.notice_iv_user_image);
            name = itemView.findViewById(R.id.notice_tv_user_name);
            dot = itemView.findViewById(R.id.notice_iv_dot);
            title = itemView.findViewById(R.id.notice_tv_title);
            content = itemView.findViewById(R.id.notice_tv_content);
            iv_msg = itemView.findViewById(R.id.notice_iv_message);
            count = itemView.findViewById(R.id.comment_count);
            line = itemView.findViewById(R.id.view_line);

            // 아이템 클릭 이벤트 추가
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체 메서드 호출
                        if(onItemClickListener != null) {
                            onItemClickListener.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
