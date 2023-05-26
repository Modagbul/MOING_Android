package com.example.moing.mypage;


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
import com.example.moing.response.MyPageResponse;
import com.example.moing.s3.DownloadImageCallback;
import com.example.moing.s3.S3Utils;

import java.util.List;

public class MyPageTeamAdatper extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final Context context;
    private final List<MyPageResponse.Team> teamList;

    public MyPageTeamAdatper(Context context, List<MyPageResponse.Team> teamList) {
        this.context = context;
        this.teamList = teamList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ViewHolder 생성
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recycler_item_my_page_team,parent,false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyPageTeamAdatper.TeamViewHolder teamViewHolder = (MyPageTeamAdatper.TeamViewHolder) holder;
        MyPageResponse.Team team = teamList.get(position);

        // 소모임 사진 설정
        S3Utils.downloadImageFromS3(team.getProfileUrl(), new DownloadImageCallback() {
            @Override
            public void onImageDownloaded(byte[] data) {
                runOnUiThread(() -> Glide.with(context)
                        .asBitmap()
                        .load(data)
                        .transform(new RoundedCorners(24))
                        .into(teamViewHolder.ivProfile));
            }
            @Override
            public void onImageDownloadFailed() {
            }
        });

        // 소모임 사진 틴트 컬러 설정
        int color = Color.parseColor("#000000");  // 원래 색상
        int alpha = (int) (255 * 0.48);  // 48%의 알파 값
        int tintedColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
        teamViewHolder.ivProfile.setColorFilter(tintedColor);

        // 소모임 이름 설정
        teamViewHolder.tvName.setText(team.getTeamName());

    }

    @Override
    public int getItemCount() { return teamList.size(); }

    class TeamViewHolder extends RecyclerView.ViewHolder{
        private final ImageView ivProfile;
        private final TextView tvName;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            // 소모임 사진
            ivProfile = itemView.findViewById(R.id.mypage_team_iv_profile);
            // 소모임 이름
            tvName = itemView.findViewById(R.id.mypage_team_tv_name);
        }
    }
}
