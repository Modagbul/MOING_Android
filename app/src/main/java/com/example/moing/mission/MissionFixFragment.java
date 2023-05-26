package com.example.moing.mission;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

import com.example.moing.R;
import com.example.moing.response.MissionInfoResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionFixFragment extends BottomSheetDialogFragment {
    private Dialog fixDialogImpossible;
    private static final String TAG = "MissionFixFragment";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private Long teamId, missionId;

    private Call<MissionInfoResponse> call;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mission_fix, container, false);

        // Intent로 값 받기.
        teamId = getActivity().getIntent().getLongExtra("teamId", 0);
        missionId = getActivity().getIntent().getLongExtra("missionId", 0);
        /** 소모임장 번호도 가져와야 한다. **/

        // 닫기 버튼
        ImageButton close = view.findViewById(R.id.board_dot_btn_close);
        close.setOnClickListener(closeClickListener);

        // 미션 수정 버튼
        ImageButton fix = view.findViewById(R.id.mission_fix);
        fix.setOnClickListener(fixClickListener);

        // 초대 코드 팝업 다이얼로그 - 불가능
        fixDialogImpossible = new Dialog(getContext());
        fixDialogImpossible.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        fixDialogImpossible.setContentView(R.layout.fragment_board_invite_code_impossible); // xml 레이아웃 파일 연결

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(call != null){
            call.cancel();
        }
    }

    // 닫기 버튼 클릭 시
    View.OnClickListener closeClickListener = v -> dismiss();
    // 미션 수정 버튼 클릭 시 미션 수정 액티비티 실행
    View.OnClickListener fixClickListener = v -> getMissionInfo();

    /**
     * 미션 상세 조회 (소모임 정보 수정을 위한 조회, 소모임장만 가능)
     **/
    private void getMissionInfo() {
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        call = apiService.getMission(jwtAccessToken, teamId, missionId);
        call.enqueue(new Callback<MissionInfoResponse>() {
            @Override
            public void onResponse(@NonNull Call<MissionInfoResponse> call, @NonNull Response<MissionInfoResponse> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // 연결 성공 -> 소모임 정보 수정 액티비티 실행 및 값 전달
                        Log.d(TAG, response.body().toString());
                        Intent intent = new Intent(getContext(), MissionReviseActivity.class);
                        intent.putExtra("teamId",teamId);
                        intent.putExtra("missionId",missionId);
                        intent.putExtra("title", response.body().getData().getTitle());
                        intent.putExtra("dueTo", response.body().getData().getDueTo());
                        intent.putExtra("content", response.body().getData().getContent());
                        intent.putExtra("rule", response.body().getData().getRule());
                        intent.putExtra("status", response.body().getData().getStatus());

                        startActivity(intent);
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

                        if(message.equals("소모임장이 아니어서 할 수 없습니다.")) {
                            showInviteDialogImpossible();
                        }
                        else if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getContext());
                            getMissionInfo();
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
            public void onFailure(@NonNull Call<MissionInfoResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "getTeamList Fail");
            }
        });
    }

    // 초대 코드 복사 불가 팝업
    private void showInviteDialogImpossible() {
        fixDialogImpossible.show(); // 다이얼로그 띄우기
        fixDialogImpossible.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 투명 배경

        ImageButton btnClose =  fixDialogImpossible.findViewById(R.id.board_invite_code_impossible_btn_close);
        View llWhole = fixDialogImpossible.findViewById(R.id.board_invite_code_impossible_ll);

        // 터치시 종료
        llWhole.setOnClickListener(v->fixDialogImpossible.dismiss());
        btnClose.setOnClickListener(v->fixDialogImpossible.dismiss());
    }
}