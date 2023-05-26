package com.example.moing.team;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.moing.R;
import com.example.moing.response.TeamListResponse;
import com.example.moing.board.BoardActivity;
import com.example.moing.s3.DownloadImageCallback;
import com.example.moing.s3.S3Utils;

import java.util.List;

/**
 * TeamRecyclerView 의 Adapter
 **/
public class TeamAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int DEFAULT_TYPE = 0;
    private static final int TEAM_TYPE = 1;
    private static final int MAX_TEAM_NUM = 3;
    private final List<TeamListResponse.Team> teamList;
    private final Context mainContext;

    public TeamAdapter(List<TeamListResponse.Team> teamList, Context mainContext) {
        teamList.add(new TeamListResponse.Team(-1));
        this.teamList = teamList;
        this.mainContext = mainContext;
    }

    @Override
    public int getItemViewType(int position) {
        // 마지막 DefaultType
        if (position == getItemCount() - 1) {
            return DEFAULT_TYPE;
        }
        // 그 외 TeamType
        return TEAM_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // viewType 에 따라 ViewHolder 와 Layout 설정
        if (viewType == DEFAULT_TYPE) {
            view = inflater.inflate(R.layout.recycler_item_team_default, parent, false);
            viewHolder = new DefaultViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.recycler_item_team, parent, false);
            viewHolder = new TeamViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        // viewType 에 따라 text 및 image 설정
        TeamListResponse.Team team = teamList.get(position);
        switch (holder.getItemViewType()) {
            case DEFAULT_TYPE:
                DefaultViewHolder defaultViewHolder = (DefaultViewHolder) holder;
                String text = "모임 추가하기 (" + (teamList.size() - 1) + "/3)";
                defaultViewHolder.btnAddTeam.setText(text);

                if (teamList.size() - 1 == TeamAdapter.MAX_TEAM_NUM) {
                    // 최대 생성 가능 모임 (3)을 채웠을 경우 -> img 변경, 버튼 비활성화
                    defaultViewHolder.ivTeam.setBackgroundResource(R.drawable.ic_make_team_full);
                    defaultViewHolder.btnAddTeam.setClickable(false);
                    defaultViewHolder.btnAddTeam.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.secondary_grey_black_13));
                    defaultViewHolder.btnAddTeam.setTextColor(ContextCompat.getColor(mainContext, R.color.secondary_grey_black_10));
                } else {
                    // 버튼 활성화
                    defaultViewHolder.btnAddTeam.setOnClickListener(view -> {
                        Intent intent = new Intent(view.getContext(), AddTeamActivity.class);
                        view.getContext().startActivity(intent);
                    });
                }
                break;
            case TEAM_TYPE:
                TeamViewHolder teamViewHolder = (TeamViewHolder) holder;
                teamViewHolder.title.setText(team.getName());
                teamViewHolder.memberNum.setText(String.valueOf(team.getPersonnel()));
                teamViewHolder.missionStart.setText(team.getStartDate());
                teamViewHolder.missionEnd.setText(team.getEndDate());
                teamViewHolder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(view.getContext(), BoardActivity.class);
                    intent.putExtra("teamId", team.getTeamId());
                    view.getContext().startActivity(intent);
                });

                // S3 이미지 다운로드 -> 이미지 뷰에 설정
                S3Utils.downloadImageFromS3(team.getProfileImg(), new DownloadImageCallback() {
                    @Override
                    public void onImageDownloaded(byte[] data) {
                        runOnUiThread(() -> Glide.with(mainContext)
                                .asBitmap()
                                .load(data)
                                .transform(new RoundedCorners(24))
                                .into(teamViewHolder.image));
                    }

                    @Override
                    public void onImageDownloadFailed() {

                    }
                });

                break;
        }
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }


    public static class DefaultViewHolder extends RecyclerView.ViewHolder {
        Button btnAddTeam;
        ImageView ivTeam;

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAddTeam = itemView.findViewById(R.id.team_default_btn_add_team);
            ivTeam = itemView.findViewById(R.id.team_default_iv);
        }
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView memberNum;
        TextView missionStart;
        TextView missionEnd;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.team_iv_team_image);
            title = itemView.findViewById(R.id.team_tv_team_title);
            memberNum = itemView.findViewById(R.id.team_tv_team_member_num);
            missionStart = itemView.findViewById(R.id.team_tv_mission_start);
            missionEnd = itemView.findViewById(R.id.team_tv_team_mission_end);
        }
    }
}
