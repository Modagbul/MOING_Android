package com.example.moing.mission;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.moing.R;
import com.example.moing.response.MissionStatusListResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionStatusActivity extends AppCompatActivity implements Serializable {
    private static final String TAG = "MissionStatusActivity";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private TextView tvMissionTitle;
    private TextView tvRemainDay;
    private TextView tvCompleteUser;
    private ImageView ivStatus;
    private Fragment fragmentDone;
    private Fragment fragmentUnDone;
    private TabLayout tabs;

    private Long teamId;
    private Long missionId;

    private Call<MissionStatusListResponse> call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_status);

        // teamId 값 받아오기
        teamId = getIntent().getLongExtra("teamId", 0);
        // missionId 값 받아오기
        missionId = getIntent().getLongExtra("missionId", 0);
        Log.d(TAG,"teamId :"+teamId+"missionId: "+missionId);

        // 내 인증 상태 확인 컴포넌트 - 진행 상태에 따라 다르게 나옴
        ivStatus = findViewById(R.id.mission_status_iv_status); // 나의 인증 상태
        tvMissionTitle = findViewById(R.id.mission_status_tv_mission_title); // 미션 제목
        tvRemainDay = findViewById(R.id.mission_status_tv_remain_day); // 남은 시간
        tvCompleteUser = findViewById(R.id.mission_status_tv_complete_user); // 인증 완료 인원

        // 뒤로가기 - 클릭 시 종료
        ImageButton btnBack = findViewById(R.id.mission_status_btn_back);
        btnBack.setOnClickListener(view -> finish());

        // TabLayout - 인증 완료 / 인증 미완료
        tabs = findViewById(R.id.mission_status_tl);

        // 보러가기 - 미션 인증하기로 이동
        Button btnGo = findViewById(R.id.mission_status_btn_go);
        btnGo.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),MissionClickActivity.class);
            intent.putExtra("teamId",teamId);
            intent.putExtra("missionId",missionId);
            startActivity(intent);
            finish();
        });

        // 미션 목록 리스트 불러오고 설정
        getMissionStatusList(teamId,missionId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(call != null){
            call.cancel();
        }
    }

    private void getMissionStatusList(long teamId, long missionId) {
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);
        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);

        // 미션 현황 리스트를 가져옴
        call = apiService.getMissionStatusList(jwtAccessToken,teamId, missionId);
        call.enqueue(new Callback<MissionStatusListResponse>() {
            @Override
            public void onResponse(@NonNull Call<MissionStatusListResponse> call, @NonNull Response<MissionStatusListResponse> response) {
                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // 완료한 인원 목록
                        List<MissionStatusListResponse.UserMission> doneList = response.body().getData().getCompleteList();

                        // 미완료한 인원 목록, 불을 맞은 인원 목록
                        List<MissionStatusListResponse.UserMission> undoneList = response.body().getData().getIncompleteList();
                        List<Integer> fireList = response.body().getData().getFireUserMissionList();

                        // 미션 상태에 따른 컴포넌트 상단 바 설정
                        String myStatus = response.body().getData().getMyStatus();

                        // 미션 제목 설정
                        String missionTitle = response.body().getData().getTitle();

                        // 미션 남은 시간 설정
                        String remainDay = "남은 시간 D-"+response.body().getData().getRemainDay();

                        // 미션 인증 완료 인원 설정
                        long totalNum = doneList.size() + undoneList.size();   // 총 인원
                        long doneNum = doneList.size();
                        String completeUser = "인증완료 인원 " + doneNum + "/" + totalNum;

                        // UI 업데이트 작업을 예약하기 위해 핸들러를 사용
                        new Handler(Looper.getMainLooper()).post(() -> {

                            // 완료한 인원 목록
                            Bundle doneBundle = new Bundle();
                            doneBundle.putSerializable("doneList", new ArrayList<>(doneList));   // 완료한 인원 목록 저장

                            // 미완료한 인원 목록, 불을 맞은 인원 목록
                            Bundle undoneBundle = new Bundle();
                            undoneBundle.putSerializable("undoneList", new ArrayList<>(undoneList)); // 미완료한 인원 목록 저장
                            undoneBundle.putSerializable("fireList", new ArrayList<>(fireList));    // 불을 맞은 인원 목록 저장

                            // teamId, missionId 저장
                            doneBundle.putLong("teamId",teamId);
                            doneBundle.putLong("missionId",missionId);
                            undoneBundle.putLong("teamId",teamId);
                            undoneBundle.putLong("missionId",missionId);

                            // "나"를 표현하기 위한 나의 상태 체크
                            if(myStatus.equals("COMPLETE") || myStatus.equals("PENDING")){
                                // 나의 인증 현황이 완료 or 건너뛰기 - 인증 완료에 true 전달
                                doneBundle.putBoolean("isExist", true);
                                undoneBundle.putBoolean("isExist",false);
                            }
                            else{
                                // 나의 인증 현황이 미완료 - 인증 미완료에 true 전달
                                doneBundle.putBoolean("isExist", false);
                                undoneBundle.putBoolean("isExist",true);
                            }

                            // 인증 완료 Fragment
                            fragmentDone = new MissionStatusDoneFragment();
                            fragmentDone.setArguments(doneBundle);  // 완료 프래그먼트에 전달

                            // 인증 미완료 Fragment
                            fragmentUnDone = new MissionStatusUndoneFragment();
                            fragmentUnDone.setArguments(undoneBundle); // 미완료 프래그먼트에 전달

                            // FrameLayout 기본 Fragment - 인증 완료, hide(인증 미완료)
                            getSupportFragmentManager().beginTransaction()
                                    .add(R.id.mission_status_fl, fragmentDone)
                                    .add(R.id.mission_status_fl, fragmentUnDone)
                                    .hide(fragmentUnDone)
                                    .commit();

                            // TabLayout 설정
                            tabs.addTab(tabs.newTab().setText("인증완료"));
                            tabs.addTab(tabs.newTab().setText("인증미완료"));
                            tabs.addOnTabSelectedListener(onTabSelectedListener);

                            // 미션 상태에 따른 상단바 설정
                            setMyStatus(myStatus, ivStatus, tvMissionTitle);

                            // 미션 제목 설정
                            tvMissionTitle.setText(missionTitle);

                            // 미션 남은 시간 설정
                            tvRemainDay.setText(remainDay);

                            // 미션 인증 완료 인원 설정
                            tvCompleteUser.setText(completeUser);
                        });
                    }
                }
                else{
                    Log.d(TAG, response.message());
                    try {
                        String errorJson = response.errorBody().string();
                        JSONObject errorObject = new JSONObject(errorJson);
                        // 에러 코드로 에러처리를 하고 싶을 때
                        // String errorCode = errorObject.getString("errorCode");
                        /** 메세지로 에러처리를 구분 **/
                        String message = errorObject.getString("message");

                        if (message.equals("만료된 토큰입니다.")) {
                            // 토큰 재발급 후 다시 호출
                            ChangeJwt.updateJwtToken(MissionStatusActivity.this);
                            getMissionStatusList(teamId, missionId);
                        }
                        else if(message.equals("접근이 거부되었습니다.")){
                            Log.d(TAG, "접근이 거부되었습니다.");
                        }


                    } catch (IOException e) {
                        // 에러 응답의 JSON 문자열을 읽을 수 없을 때
                        e.printStackTrace();
                    } catch (JSONException e) {
                        // JSON 객체에서 필드 추출에 실패했을 때
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MissionStatusListResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "getMissionStatusList Fail");
            }
        });
    }

    TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            // 선택한 Fragment 로 교체
            int position = tab.getPosition();
            Fragment showFragment = null;
            Fragment hideFragment = null;
            switch (position) {
                case 0:
                    showFragment = fragmentDone;
                    hideFragment = fragmentUnDone;
                    break;
                case 1:
                    showFragment = fragmentUnDone;
                    hideFragment = fragmentDone;
                    break;
            }

            if (showFragment != null && hideFragment != null) {
                if(showFragment == fragmentUnDone)
                    showCustomToast();

                getSupportFragmentManager().beginTransaction()
                        .show(showFragment)
                        .hide(hideFragment)
                        .commit();
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}

        @Override
        public void onTabReselected(TabLayout.Tab tab) {}
    };

    // 유저의 인증 현황에 따른 상위 컴포넌트
    private void setMyStatus(String myStatus, ImageView ivStatus, TextView tvMissionTitle){
        // 인증 완료
        if(myStatus.equals("COMPLETE"))
        {
            ivStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_mission_status_done));
            tvMissionTitle.setPaintFlags(tvMissionTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        // 인증 건너뛰기
        else if(myStatus.equals("PENDING")){
            ivStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_mission_status_pending));
            tvMissionTitle.setPaintFlags(tvMissionTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        // 인증 미완료
        else{
            ivStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_mission_status_undone));
            tvMissionTitle.setPaintFlags(tvMissionTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

    }

    // 커스텀 Toast 메시지 표시
    public void showCustomToast() {
        // 커스텀 레이아웃 파일 인플레이션
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_mission_undone, null);

        // Toast 객체 생성 및 커스텀 레이아웃 설정
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        // Toast 메시지 표시
        toast.show();
    }

}