package com.example.moing.board;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moing.R;
import com.example.moing.response.InvitationCodeResponse;
import com.example.moing.response.TeamResponse;
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

public class BoardDotFragment extends BottomSheetDialogFragment {
    private static final String TAG = "BoardDotFragment";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private Dialog inviteDialog;
    private Dialog inviteDialogImpossible;
    private Long teamId;

    private  Call<TeamResponse> teamResponseCall;
    private Call<InvitationCodeResponse> invitationCodeResponseCall;

    public BoardDotFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board_dot, container, false);

        // Intent로 값 받기
        teamId = getActivity().getIntent().getLongExtra("teamId", 0);

        // 닫기 버튼
        ImageButton btnClose = view.findViewById(R.id.board_dot_btn_close);
        btnClose.setOnClickListener(closeClickListener);

        // 초대 코드 버튼
        ImageButton btnLink = view.findViewById(R.id.board_dot_btn_link);
        btnLink.setOnClickListener(linkClickListener);

        // 소모임 수정 버튼
        ImageButton btnFix = view.findViewById(R.id.board_dot_btn_fix);
        btnFix.setOnClickListener(fixClickListener);

        // 초대 코드 팝업 다이얼로그 - 가능
        inviteDialog = new Dialog(getContext());
        inviteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        inviteDialog.setContentView(R.layout.fragment_board_invite_code_default); // xml 레이아웃 파일 연결

        // 초대 코드 팝업 다이얼로그 - 불가능
        inviteDialogImpossible = new Dialog(getContext());
        inviteDialogImpossible.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        inviteDialogImpossible.setContentView(R.layout.fragment_board_invite_code_impossible); // xml 레이아웃 파일 연결

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(teamResponseCall != null){
            teamResponseCall.cancel();
        }

        if(invitationCodeResponseCall != null){
            invitationCodeResponseCall.cancel();
        }
    }

    // 뒤로 가기
    View.OnClickListener closeClickListener = v -> dismiss();

    // 초대 코드 복사 팝업 띄우기
    View.OnClickListener linkClickListener = v -> {
        getInvitationCode();
        dismiss();
    };

    // 소모임 정보 수정 액티비티 실행
    View.OnClickListener fixClickListener = v -> getTeamInfo();

    // 초대 코드 복사 팝업
    private void showInviteDialog(String invitationCode) {
        TextView tvInviteCode = inviteDialog.findViewById(R.id.board_invite_code_tv_code);
        ImageButton btnClose = inviteDialog.findViewById(R.id.board_invite_code_btn_close);
        ImageButton btnPaste = inviteDialog.findViewById(R.id.board_invite_code_btn_paste);

        tvInviteCode.setText(invitationCode);

        inviteDialog.show(); // 다이얼로그 띄우기
        inviteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 투명 배경

        // 창 닫기
        btnClose.setOnClickListener(v -> inviteDialog.dismiss());

        // 복사 후 창 닫기
        btnPaste.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("invite_code", tvInviteCode.getText());
            clipboardManager.setPrimaryClip(clipData);
            inviteDialog.dismiss();
        });
    }

    // 초대 코드 복사 불가 팝업
    private void showInviteDialogImpossible() {
        inviteDialogImpossible.show(); // 다이얼로그 띄우기
        inviteDialogImpossible.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 투명 배경

        ImageButton btnClose = inviteDialogImpossible.findViewById(R.id.board_invite_code_impossible_btn_close);
        View llWhole = inviteDialogImpossible.findViewById(R.id.board_invite_code_impossible_ll);

        // 터치시 종료
        llWhole.setOnClickListener(v -> inviteDialogImpossible.dismiss());
        btnClose.setOnClickListener(v -> inviteDialogImpossible.dismiss());
    }

    /**
     * 소모임 정보 조회 (소모임 정보 수정을 위한 조회, 소모임장만 가능)
     **/
    private void getTeamInfo() {
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        teamResponseCall = apiService.getTeam(jwtAccessToken, teamId);
        teamResponseCall.enqueue(new Callback<TeamResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamResponse> call, @NonNull Response<TeamResponse> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // 연결 성공 -> 소모임 정보 수정 액티비티 실행 및 값 전달
                        Log.d(TAG, response.body().toString());
                        Intent intent = new Intent(getContext(), BoardFixTeamActivity.class);
                        intent.putExtra("teamId",teamId);
                        intent.putExtra("name", response.body().getData().getName());
                        intent.putExtra("endDate", response.body().getData().getEndDate());
                        intent.putExtra("profileImg", response.body().getData().getProfileImg());

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
                            getTeamInfo();
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
            public void onFailure(@NonNull Call<TeamResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "getTeamList Fail");
            }
        });
    }

    /**
     * 소모임 정보 조회 (소모임 정보 수정을 위한 조회, 소모임장만 가능)
     **/
    private void getInvitationCode() {
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        invitationCodeResponseCall = apiService.getInvitationCode(jwtAccessToken, teamId);
        invitationCodeResponseCall.enqueue(new Callback<InvitationCodeResponse>() {
            @Override
            public void onResponse(@NonNull Call<InvitationCodeResponse> call, @NonNull Response<InvitationCodeResponse> response) {
                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, response.body().toString());
                        // 초대 코드 전달 (팝업 띄우기)
                        showInviteDialog(response.body().getData().getInvitationCode());

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
                            getInvitationCode();
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
            public void onFailure(@NonNull Call<InvitationCodeResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "getTeamList Fail");
            }
        });
    }
}