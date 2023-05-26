package com.example.moing.team;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moing.R;
import com.example.moing.response.InviteTeamResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteTeamActivity extends AppCompatActivity {
    private static final String TAG = "InviteTeamActivity";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    ImageButton btn_back;
    EditText et_code;
    Button btn_check, btn_realCheck;
    TextView tv_code, tv3, tv4;
    public String checkCode;

    private Call<InviteTeamResponse> call;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_team);

        btn_back = (ImageButton) findViewById(R.id.btn_back);
        et_code = (EditText) findViewById(R.id.et_code);
        btn_check = (Button) findViewById(R.id.btn_check);
        btn_realCheck = (Button) findViewById(R.id.btn_realCheck);
        tv_code = (TextView) findViewById(R.id.tv_code);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv4 = (TextView) findViewById(R.id.tv4);

        // 뒤로 가기 버튼
        btn_back.setOnClickListener(v -> finish());

        // 버튼 먼저 눌렀을 때 참여 코드 인증해달라고 Toast 문구 띄우기
        btn_check.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "참여 코드를 입력해주세요.", Toast.LENGTH_SHORT).show());

        // 코드 입력 버튼 눌렀을 때 참여코드 뜨게 하기
        et_code.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                tv3.setVisibility(View.INVISIBLE);
                tv_code.setVisibility(View.VISIBLE);
                btn_check.setClickable(false);
                btn_check.setVisibility(View.INVISIBLE);
                btn_realCheck.setVisibility(View.VISIBLE);
            }
            return false;
        });

        // 한글자 이상 입력시 참여코드 뜨게 하기
        et_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_realCheck.setBackgroundResource(R.drawable.button_round_black1);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // 참여코드 인증버튼 클릭
        btn_realCheck.setOnClickListener(v -> {
            // POST 통신 후 값이 맞다면 인증화면으로 넘어가고, 아니면 빨간색으로 해주는 처리 해줘야된다.
            // 지금은 다음 페이지 구현을 위해 임의로 값 지정하여 넘어간다 !!
            checkCode = et_code.getText().toString();
            putTeamUpdate(checkCode);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel(); // API 요청 취소
        }
    }

    /**
     * 입력한 초대코드 전달 -> 성공시 소모임 가입 완료
     **/
    private void putTeamUpdate(String invitationCode) {
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        call = apiService.postAuthInvitationCode(jwtAccessToken,invitationCode);
        call.enqueue(new Callback<InviteTeamResponse>() {
            @SuppressLint("ShowToast")
            @Override
            public void onResponse(@NonNull Call<InviteTeamResponse> call, @NonNull Response<InviteTeamResponse> response) {
                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, response.body().toString());
                        Long teamId = response.body().getData().getTeamId();
                        String profileImg = response.body().getData().getProfileImg();
                        // 인증 완료
                        Intent intent = new Intent(getApplicationContext(), InviteSuccessTeamActivity.class);
                        intent.putExtra("teamId", teamId);
                        intent.putExtra("profileImg", profileImg);
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

                        if(message.equals("이미 해당 소모임에 참여했습니다")) {
                            Toast.makeText(getApplicationContext(), "이미 해당 소모임에 참여했습니다",Toast.LENGTH_SHORT).show();
                        }
                        else if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getApplicationContext());
                            putTeamUpdate(invitationCode);
                        }
                        else if(message.equals("참여코드 값이 잘못되었습니다")){
                            Toast.makeText(getApplicationContext(), "참여코드 값이 잘못되었습니다",Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        // 에러 응답의 JSON 문자열을 읽을 수 없을 때
                        e.printStackTrace();
                    } catch (JSONException e) {
                        // JSON 객체에서 필드 추출에 실패했을 때
                        e.printStackTrace();
                    }

                    tv_code.setTextColor(ContextCompat.getColorStateList(InviteTeamActivity.this, R.color.secondary_grey_black_7));
                    et_code.setBackgroundResource(R.drawable.edittext_checkcode_result_false);
                    tv4.setVisibility(View.VISIBLE);
                    et_code.setText(null);
                    btn_realCheck.setBackgroundResource(R.drawable.button_round_black12);
                }
            }
            @Override
            public void onFailure(@NonNull Call<InviteTeamResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "실패");
            }
        });
    }
}