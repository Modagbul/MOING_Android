package com.example.moing.mission;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.moing.R;
import com.example.moing.response.MissionStatusListResponse;
import com.example.moing.s3.DownloadImageCallback;
import com.example.moing.s3.S3Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MissionStatusDoneFragment extends Fragment {
    private static final int DONE_TYPE = 0;
    private static final int PENDING_TYPE = 1;
    private Dialog doneDialog;
    private Dialog pendingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission_status_done, container, false);

        // MissionStatusActivity에서 전달받은 인증 완료 리스트
        Bundle bundle = getArguments();
        ArrayList<MissionStatusListResponse.UserMission> doneList = (ArrayList<MissionStatusListResponse.UserMission>) (bundle != null ? bundle.getSerializable("doneList") : null);

        // "나"를 표현하기 위한 나의 상태
        boolean isExist = bundle.getBoolean("isExist");

        // 리사이클러뷰
        RecyclerView recyclerView = view.findViewById(R.id.mission_status_done_rv);

        // 리사이클러뷰에 사용할 레이아웃매니저 - Grid 2개의 열
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        // 리사이클러뷰 아이템들 간 간격 설정
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 24, 0, 24);
            }
        });

        // 리사이클러뷰에 사용할 어댑터 설정
        MissionDoneAdapter adapter = new MissionDoneAdapter(doneList,getContext(),isExist);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((status, mission) -> {
            // 아이템 클릭 시 팝업 창을 띄우는 로직
            switch (status) {
                case DONE_TYPE:
                    showDoneDialog(mission);
                    break;
                case PENDING_TYPE:
                    showPendingDialog(mission);
                    break;
            }

        });

        // 미션 완료 다이얼로그
        doneDialog = new Dialog(getContext());
        doneDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        doneDialog.setContentView(R.layout.fragment_mission_status_done_popup); // xml 레이아웃 파일 연결

        // 미션 건너뛰기 다이얼로그
        pendingDialog = new Dialog(getContext());
        pendingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        pendingDialog.setContentView(R.layout.fragment_mission_status_pending_popup); // xml 레이아웃 파일 연결

        TextView tvNum = view.findViewById(R.id.mission_status_done_tv_num);
        int size = doneList != null ? doneList.size() : 0;  // 완료 인원 명수
        // 완료 인원 표시
        String text = size +"명";
        tvNum.setText(text);

        // 완료 0 명 - 모두 미완료인 경우 이미지
        ImageView ivDoneAll = view.findViewById(R.id.mission_status_done_iv_undone_all);
        if(size == 0)
            ivDoneAll.setVisibility(View.VISIBLE);
        else
            ivDoneAll.setVisibility(View.GONE);


        return view;
    }

    // 미션 완료 다이얼로그 띄우기
    private void showDoneDialog(MissionStatusListResponse.UserMission mission){

        ImageButton btnClose = doneDialog.findViewById(R.id.mission_status_done_popup_btn_close);   // 창 닫기
        ImageView ivProfile = doneDialog.findViewById(R.id.mission_status_done_popup_iv_profile);   // 프로필 이미지
        ImageView ivImage = doneDialog.findViewById(R.id.mission_status_done_popup_iv_image);       // 미션 인증 이미지
        TextView tvNickname = doneDialog.findViewById(R.id.mission_status_done_popup_tv_nickname);  // 닉네임
        TextView tvDate = doneDialog.findViewById(R.id.mission_status_done_popup_tv_date);          // 인증한 날짜

        // 창 닫기 클릭 리스너 설정
        btnClose.setOnClickListener(v -> doneDialog.dismiss());

        // 프로필 이미지 설정
        Glide.with(getContext())
                .load(mission.getProfileImg())
                .into(ivProfile);

        // 미션 인증 사진 설정
        S3Utils.downloadImageFromS3(mission.getArchive(), new DownloadImageCallback() {
            @Override
            public void onImageDownloaded(byte[] data) {
                runOnUiThread(() -> Glide.with(getContext())
                        .asBitmap()
                        .load(data)
                        .transform(new RoundedCorners(24))
                        .into(ivImage));
            }
            @Override
            public void onImageDownloadFailed() {

            }
        });

        // 프로필 이미지 설정
        Glide.with(getContext())
                .load(mission.getProfileImg())
                .into(ivProfile);

        // 닉네임 설정
        tvNickname.setText(mission.getNickname());

        // 인증한 날짜 설정
        String inputDate = mission.getSubmitDate();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = inputFormat.parse(inputDate);
            String formattedDate = outputFormat.format(date);
            tvDate.setText(formattedDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        doneDialog.show(); // 다이얼로그 띄우기
        doneDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 투명 배경
    }

    // 미션 건너뛰기 다이얼로그 띄우기
    private void showPendingDialog(MissionStatusListResponse.UserMission mission){

        ImageButton btnClose = pendingDialog.findViewById(R.id.mission_status_pending_popup_btn_close); // 창 닫기
        ImageView ivProfile = pendingDialog.findViewById(R.id.mission_status_pending_popup_iv_profile); // 프로필 이미지
        TextView tvReason = pendingDialog.findViewById(R.id.mission_status_pending_popup_tv_reason);    // 미션 건너뛰기 사유
        TextView tvNickname = pendingDialog.findViewById(R.id.mission_status_pending_popup_tv_nickname);// 닉네임
        TextView tvDate = pendingDialog.findViewById(R.id.mission_status_pending_popup_tv_date);        // 인증한 날짜

        // 창 닫기 클릭 리스너 설정
        btnClose.setOnClickListener(v -> pendingDialog.dismiss());

        // 프로필 이미지 설정
        Glide.with(getContext())
                .load(mission.getProfileImg())
                .into(ivProfile);

        // 미션 건너뛰기 사유 설정
        tvReason.setText(mission.getArchive());

        // 닉네임 설정
        tvNickname.setText(mission.getNickname());

        // 인증한 날짜 설정
        String inputDate = mission.getSubmitDate();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = inputFormat.parse(inputDate);
            String formattedDate = outputFormat.format(date);
            tvDate.setText(formattedDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        pendingDialog.show(); // 다이얼로그 띄우기
        pendingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 투명 배경

    }

}