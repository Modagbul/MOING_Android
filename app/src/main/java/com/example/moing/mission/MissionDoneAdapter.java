package com.example.moing.mission;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.content.Context;
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

public class MissionDoneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int DONE_TYPE = 0;
    private static final int PENDING_TYPE = 1;
    private ArrayList<MissionStatusListResponse.UserMission> missionList;

    private OnMissionClicklistener onMissionDoneClicklistener;
    private final Context mainContext;
    private final boolean isExist;

    public MissionDoneAdapter(ArrayList<MissionStatusListResponse.UserMission> missionList, Context context, boolean isExist) {
        this.missionList = missionList;
        this.mainContext = context;
        this.isExist = isExist;
    }

    public interface OnMissionClicklistener {
        void onItemClick(int status, MissionStatusListResponse.UserMission mission);
    }

    public void setOnItemClickListener(OnMissionClicklistener listener) {
        this.onMissionDoneClicklistener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        // COMPLETE -> 완료
        if (missionList.get(position).getStatus().equals("COMPLETE")) {
            return DONE_TYPE;
        }
        // PENDING -> 건너뛰기
        return PENDING_TYPE;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // viewType 에 따라 ViewHolder 와 Layout 설정
        if(viewType == DONE_TYPE){
            // 완료 상태
            view = inflater.inflate(R.layout.recycler_item_mission_done,parent,false);
            viewHolder = new MissionDoneAdapter.DoneViewHolder(view);
        }
        else{
            // 건너뛰기 상태
            view = inflater.inflate(R.layout.recycler_item_mission_pending,parent,false);
            viewHolder = new MissionDoneAdapter.PendingViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // viewType 에 따라 text 및 image 설정
        MissionStatusListResponse.UserMission mission = missionList.get(position);

        switch (holder.getItemViewType()) {
            case DONE_TYPE:
                // 완료에 대한 리사이클러뷰들을 받아와 처리
                MissionDoneAdapter.DoneViewHolder doneViewHolder = (DoneViewHolder) holder;

                // 아이템 클릭 리스너 설정
                doneViewHolder.itemView.setOnClickListener(v ->{
                    if(onMissionDoneClicklistener != null)
                        onMissionDoneClicklistener.onItemClick(DONE_TYPE,mission);
                });

                // 첫번째는 나 표시
                if(position == 0 && isExist)
                    doneViewHolder.ivMe.setVisibility(View.VISIBLE);

                // 미션 인증 사진 설정
                S3Utils.downloadImageFromS3(mission.getArchive(), new DownloadImageCallback() {
                    @Override
                    public void onImageDownloaded(byte[] data) {
                        runOnUiThread(() -> Glide.with(mainContext)
                                .asBitmap()
                                .load(data)
                                .transform(new RoundedCorners(24))
                                .into(doneViewHolder.ivArchive));
                    }
                    @Override
                    public void onImageDownloadFailed() {

                    }
                });

                // 프로필 이미지 설정
                S3Utils.downloadImageFromS3(mission.getProfileImg(), new DownloadImageCallback() {
                    @Override
                    public void onImageDownloaded(byte[] data) {
                        runOnUiThread(() -> Glide.with(mainContext)
                                .asBitmap()
                                .load(data)
                                .into(doneViewHolder.ivProfile));
                    }
                    @Override
                    public void onImageDownloadFailed() {
                        runOnUiThread(() -> Glide.with(mainContext)
                                .load(mission.getProfileImg())
                                .into(doneViewHolder.ivProfile));
                    }
                });

                // 닉네임 설정
                doneViewHolder.tvNickname.setText(mission.getNickname());

                break;
            case PENDING_TYPE:
                // 건너뛰기에 대한 리사이클러뷰들을 받아와 처리
                MissionDoneAdapter.PendingViewHolder pendingViewHolder = (PendingViewHolder) holder;

                // 아이템 클릭 리스너 설정
                pendingViewHolder.itemView.setOnClickListener(v ->{
                    if(onMissionDoneClicklistener != null)
                        onMissionDoneClicklistener.onItemClick(PENDING_TYPE,mission);
                });

                // 첫번째는 나 표시
                if(position == 0 && isExist)
                    pendingViewHolder.ivMe.setVisibility(View.VISIBLE);

                // 미션 건너뛰기 이유 설정
                pendingViewHolder.tvReason.setText(mission.getArchive());

                // 프로필 이미지 설정
                Glide.with(mainContext)
                        .load(mission.getProfileImg())
                        .into(pendingViewHolder.ivProfile);

                // 닉네임 설정
                pendingViewHolder.tvNickname.setText(mission.getNickname());

                break;
        }
    }

    @Override
    public int getItemCount() {
        return missionList.size();
    }

    class DoneViewHolder extends RecyclerView.ViewHolder{
        ImageView ivArchive;
        CircleImageView ivProfile;
        ImageView ivMe;
        TextView tvNickname;

        public DoneViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArchive = itemView.findViewById(R.id.mission_done_iv_archive);
            ivProfile = itemView.findViewById(R.id.mission_done_iv_profile);
            ivMe = itemView.findViewById(R.id.mission_done_iv_me);
            tvNickname = itemView.findViewById(R.id.mission_done_tv_nickname);
        }
    }

    class PendingViewHolder extends RecyclerView.ViewHolder{
        CircleImageView ivProfile;
        ImageView ivMe;
        TextView tvReason;
        TextView tvNickname;

        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.mission_pending_iv_profile);
            ivMe = itemView.findViewById(R.id.mission_pending_iv_me);
            tvReason = itemView.findViewById(R.id.mission_pending_tv_reason);
            tvNickname = itemView.findViewById(R.id.mission_pending_tv_nickname);

        }
    }
}
