package com.example.moing.mission;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moing.R;
import com.example.moing.response.MissionListResponse;

import java.util.List;

public class MissionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MissionListResponse.MissionData> listData;
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

    public MissionListAdapter(List<MissionListResponse.MissionData> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_item_mission, parent, false);
        return new MissionListAdapter.MissionListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MissionListResponse.MissionData item = listData.get(position);
        MissionListHolder vh = (MissionListHolder) holder;

        vh.tv_title.setText(item.getTitle()); // 제목
        vh.tv_date.setText("남은시간 D - " + item.getDueTo()); // 날짜

        String dueTo = item.getDueTo();
        if (dueTo != null && dueTo.startsWith("-")) {
            // 값이 음수인 경우 처리
            if (item.getStatus().equals("COMPLETE")) {
                vh.btn_check.setBackgroundResource(R.drawable.success_badge);
                vh.tv_title.setPaintFlags(vh.tv_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                vh.tv_title.setTextColor(Color.parseColor("#959698"));
                vh.tv_date.setTextColor(Color.parseColor("#959698"));
                vh.tv_date.setText("마감");
            } else if (item.getStatus().equals("PENDING")){
                vh.btn_check.setBackgroundResource(R.drawable.success_badge);
                vh.tv_title.setPaintFlags(vh.tv_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                vh.tv_title.setTextColor(Color.parseColor("#959698"));
                vh.tv_date.setTextColor(Color.parseColor("#959698"));
                vh.tv_date.setText("마감");
            } else {
                vh.btn_check.setBackgroundResource(R.drawable.failed_badge);
                vh.tv_title.setPaintFlags(vh.tv_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                vh.tv_title.setTextColor(Color.parseColor("#959698"));
                vh.tv_date.setTextColor(Color.parseColor("#959698"));
                vh.tv_date.setText("마감");
            }
        } else {
            // 값이 음수가 아닌 경우 처리
            if (item.getStatus().equals("COMPLETE")) {
                vh.btn_check.setBackgroundResource(R.drawable.mission_success);
                vh.tv_title.setPaintFlags(vh.tv_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                vh.tv_title.setTextColor(Color.parseColor("#959698"));
                vh.tv_date.setPaintFlags(vh.tv_date.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                vh.tv_date.setTextColor(Color.parseColor("#959698"));
            } else if (item.getStatus().equals("PENDING")){
                vh.btn_check.setBackgroundResource(R.drawable.success_badge);
                vh.tv_title.setPaintFlags(vh.tv_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                vh.tv_title.setTextColor(Color.parseColor("#959698"));
                vh.tv_date.setTextColor(Color.parseColor("#959698"));
                vh.tv_date.setText("마감");
            } else {
                vh.btn_check.setBackgroundResource(R.drawable.mission_not);
                vh.tv_title.setPaintFlags(vh.tv_title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                vh.tv_title.setTextColor(Color.parseColor("#FFFFFF"));
                vh.tv_date.setPaintFlags(vh.tv_date.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                vh.tv_date.setTextColor(Color.parseColor("#FFFFFF"));
            }
        }
        // 아이템 클릭 이벤트 추가
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    // 리스너 객체 메서드 호출
                    if(onItemClickListener != null) {
                        onItemClickListener.onItemClick(pos);
                    }
                }
            }
        });

        // 아이템 클릭 이벤트 처리
//        vh.btn_check.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int pos = vh.getAdapterPosition();
//                if (pos != RecyclerView.NO_POSITION) {
//                    MissionListResponse.MissionData clickedItem = listData.get(pos);
//                    if (clickedItem.getStatus().equals("COMPLETE")) {
//                        clickedItem.setStatus("INCOMPLETE");  // 상태를 INCOMPLETE로 변경
//                        vh.btn_check.setBackgroundResource(R.drawable.mission_not);  // 이미지를 mission_not으로 변경
//                        vh.tv_title.setPaintFlags(vh.tv_title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
//                        vh.tv_title.setTextColor(Color.parseColor("#FFFFFF"));
//                        vh.tv_date.setPaintFlags(vh.tv_date.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
//                        vh.tv_date.setTextColor(Color.parseColor("#FFFFFF"));
//
//                    } else {
//                        clickedItem.setStatus("COMPLETE");  // 상태를 COMPLETE로 변경
//                        vh.btn_check.setBackgroundResource(R.drawable.mission_success);  // 이미지를 mission_success로 변경
//                        vh.tv_title.setPaintFlags(vh.tv_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                        vh.tv_title.setTextColor(Color.parseColor("#959698"));
//                        vh.tv_date.setPaintFlags(vh.tv_date.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                        vh.tv_date.setTextColor(Color.parseColor("#959698"));
//                    }
//                    // 리스너 객체 메서드 호출
//                    if (onItemClickListener != null) {
//                        onItemClickListener.onItemClick(pos);
//                    }
//                }
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class MissionListHolder extends RecyclerView.ViewHolder {

        ConstraintLayout background;
        ImageView btn_check;
        TextView tv_title, tv_date;

        public MissionListHolder(@NonNull View itemView) {
            super(itemView);

//            background = itemView.findViewById(R.id.recycle_background);
            btn_check = itemView.findViewById(R.id.btn_check);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_date = itemView.findViewById(R.id.tv_date);


        }
    }
}
