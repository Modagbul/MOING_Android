package com.example.moing.mission;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.moing.R;
import com.example.moing.response.MissionStatusListResponse;
import com.example.moing.s3.DownloadImageCallback;
import com.example.moing.s3.S3Utils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MissionUndoneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<MissionStatusListResponse.UserMission> missionList;
    private ArrayList<Integer> fireList;
    private OnMissionUndoneClickListener onMissionUndoneClickListener;
    private Context mainContext;
    private final boolean isExist;

    public MissionUndoneAdapter(ArrayList<MissionStatusListResponse.UserMission> missionList, ArrayList<Integer> fireList, Context context, boolean isExist) {
        this.missionList = missionList;
        this.fireList = fireList;
        this.mainContext = context;
        this.isExist = isExist;
    }

    public void setFireList(ArrayList<Integer> fireList) {
        this.fireList = fireList;
    }

    public interface OnMissionUndoneClickListener {
        void onItemClick(MissionUndoneAdapter.UndoneViewHolder holder, int position, MissionStatusListResponse.UserMission mission);
    }

    public void setOnItemClickListener(OnMissionUndoneClickListener listener) {
        this.onMissionUndoneClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recycler_item_mission_undone,parent,false);
        UndoneViewHolder viewHolder = new MissionUndoneAdapter.UndoneViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UndoneViewHolder undoneViewHolder = (MissionUndoneAdapter.UndoneViewHolder) holder;

        MissionStatusListResponse.UserMission mission = missionList.get(position);

        //닉네임 설정
        undoneViewHolder.tvNickname.setText(mission.getNickname());

        // 프로필 이미지 설정
        S3Utils.downloadImageFromS3(mission.getProfileImg(), new DownloadImageCallback() {
            @Override
            public void onImageDownloaded(byte[] data) {
                runOnUiThread(() -> Glide.with(mainContext)
                        .asBitmap()
                        .load(data)
                        .transform(new RoundedCorners(24))
                        .into(undoneViewHolder.ivProfile));
            }
            @Override
            public void onImageDownloadFailed() {
                runOnUiThread(() -> Glide.with(mainContext)
                        .load(mission.getProfileImg())
                        .into(undoneViewHolder.ivProfile));
            }
        });

        // 불 맞은 사람 - 표시
        if(fireList.contains(mission.getUserMissionId())){
            // 맞은 사람 표시
            int color = Color.parseColor("#2C0E0E");  // 원래 색상
            int alpha = (int) (255 * 0.61);  // 48%의 알파 값
            int tintedColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
            undoneViewHolder.ivProfile.setColorFilter(tintedColor);
            undoneViewHolder.ivFire.setVisibility(View.VISIBLE);
            undoneViewHolder.itemView.setClickable(false);
        }
        // 불 안맞은 사람 - 미완료 리사이클러뷰 클릭 리스너 설정
        else{
            if(position != 0){
                undoneViewHolder.itemView.setOnClickListener(v ->{
                    if(onMissionUndoneClickListener != null){
                        onMissionUndoneClickListener.onItemClick(undoneViewHolder,position,mission);
                    }
                });
            }
        }

        // 첫번째는 나 표시
        if(position == 0 && isExist)
            undoneViewHolder.ivMe.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {return missionList.size();}

    class UndoneViewHolder extends RecyclerView.ViewHolder{
        CircleImageView ivProfile;
        TextView tvNickname;
        ImageView ivFire;
        ImageView ivMe;

        public UndoneViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.mission_undone_iv_profile);
            tvNickname = itemView.findViewById(R.id.mission_undone_tv_nickname);
            ivFire = itemView.findViewById(R.id.mission_undone_iv_fire);
            ivMe = itemView.findViewById(R.id.mission_undone_iv_me);
        }
    }
}
