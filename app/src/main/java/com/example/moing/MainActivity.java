package com.example.moing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.moing.response.TeamListResponse;
import com.example.moing.mypage.MyPageActivity;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;
import com.example.moing.team.TeamAdapter;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private List<TeamListResponse.Team> teamList;
    private ViewPager2 viewpager;
    private DotsIndicator dotsIndicator;
    private TextView tvCurTeam;
    private TextView tvNickName;
    private ImageButton btnMyPage;

    private  Call<TeamListResponse> call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 유저 닉네임
        tvNickName = findViewById(R.id.main_tv_name);
        // 현재 소모임 수
        tvCurTeam = findViewById(R.id.main_tv_cur_team);
        // 소모임 목록 ViewPager
        viewpager = findViewById(R.id.main_vp_team_list);
        // DotIndicator 설정
        dotsIndicator = findViewById(R.id.main_dots_indicator);
        // 마이페이지 버튼 설정
        btnMyPage = findViewById(R.id.main_btn_my_page);
        // 소모임 리스트 get
        setTeamList();

    }

    View.OnClickListener onMyPageClickListener = (v -> {
        Intent intent = new Intent(this, MyPageActivity.class);
        startActivity(intent);
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (call != null)
            call.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTeamList();
    }


    private void setTeamList() {
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);
        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        call = apiService.getTeamList(jwtAccessToken);
        call.enqueue(new Callback<TeamListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamListResponse> call, @NonNull Response<TeamListResponse> response) {
                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // 닉네임 설정
                        tvNickName.setText(response.body().getData().getUserNickName());

                        // 모임 리스트 가져오기
                        teamList = response.body().getData().getTeamBlocks();

                        // 진행 중인 모임 표시
                        String curTeamText = "진행 중 모임 " + teamList.size();
                        tvCurTeam.setText(curTeamText);

                        // adapter 설정
                        TeamAdapter teamAdapter = new TeamAdapter(teamList, getApplicationContext());
                        viewpager.setAdapter(teamAdapter);

                        // DotIndicator 설정
                        dotsIndicator.setViewPager2(viewpager);

                        // 마이페이지 버튼 클릭 리스너 설정
                        btnMyPage.setOnClickListener(onMyPageClickListener);

                    }
                } else {
                    try {
                        /** 작성자가 아닌 경우 **/
                        String errorJson = response.errorBody().string();
                        JSONObject errorObject = new JSONObject(errorJson);
                        // 에러 코드로 에러처리를 하고 싶을 때
                        // String errorCode = errorObject.getString("errorCode");
                        /** 메세지로 에러처리를 구분 **/
                        String message = errorObject.getString("message");

                        if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getApplicationContext());
                            setTeamList();
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
            public void onFailure(@NonNull Call<TeamListResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "getTeamList Fail");
            }
        });
    }
}