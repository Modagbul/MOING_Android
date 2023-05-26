package com.example.moing.mission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.moing.R;
import com.example.moing.response.MissionSkipResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionPassActivity extends AppCompatActivity {
    private static final String TAG = "MissionPassActivity";

    Button close, complete;
    TextView reason, cnt;
    EditText why;

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private SharedPreferences sharedPreferences;

    private Long teamId, missionId;

    private Call<MissionSkipResponse> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_pass);

        // Token을 사용할 SharedPreference
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Intent 값 전달받는다.
        Intent intent = getIntent();
        teamId = intent.getLongExtra("teamId", 0);
        missionId = intent.getLongExtra("missionId", 0);

        close = findViewById(R.id.btn_close);
        complete = findViewById(R.id.btn_complete);
        reason = findViewById(R.id.tv2);
        cnt = findViewById(R.id.tv_count);
        why = findViewById(R.id.et_reason);

        //Focus, TextWatcher 등록
        setFoucsChanged(why, reason,cnt);
        setTextWatcher(why, cnt);

        // 클릭 리스너
        close.setOnClickListener(closeClickListener);
        complete.setOnClickListener(completeClickListener);
        complete.setClickable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(call != null){
            call.cancel();
        }
    }

    // 뒤로가기 버튼 눌렀을 때
    View.OnClickListener closeClickListener = v -> {
        // 메인 액티비티로 다시 돌아갈 때 입력필드의 입력 값을 되돌려준다.
        Intent intent = new Intent();
        intent.putExtra("value", "0");
        setResult(RESULT_OK, intent);
        finish();
    };

    // 업로드하기 버튼 눌렀을 때
    View.OnClickListener completeClickListener = v -> {
        /** API 호출 **/
        skipMission();
    };

    /** 입력 시 값 변경 메서드 **/
    private void setTextWatcher(EditText et, TextView tv) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = "(" + s.length() + "/" + 50 + ")";
                tv.setText(result);

                if(s.length() > 0) {
                    complete.setClickable(true);
                    complete.setBackgroundColor(ContextCompat.getColor(MissionPassActivity.this, R.color.secondary_grey_black_1));
                    complete.setTextColor(ContextCompat.getColorStateList(getApplicationContext(), R.color.secondary_grey_black_12));
                }
                else {
                    complete.setClickable(false);
                    complete.setBackgroundColor(ContextCompat.getColor(MissionPassActivity.this, R.color.secondary_grey_black_12));
                    complete.setTextColor(ContextCompat.getColorStateList(getApplicationContext(), R.color.secondary_grey_black_10));
                }
            }
        });
    }

    private void setFoucsChanged(EditText et, TextView tv, TextView tv2) {
        et.setOnFocusChangeListener((view, isFocused)-> {
            if(isFocused) {
                reason.setTextColor(ContextCompat.getColor(this,R.color.main_dark_200));
                cnt.setTextColor(ContextCompat.getColor(this,R.color.main_dark_200));
                reason.setTextColor(ContextCompat.getColor(this,R.color.main_dark_200));
            }
            else {
                reason.setTextColor(ContextCompat.getColor(this,R.color.secondary_grey_black_7));
                cnt.setTextColor(ContextCompat.getColor(this,R.color.secondary_grey_black_7));
                reason.setTextColor(ContextCompat.getColor(this,R.color.secondary_grey_black_7));
            }
        });
    }

    // 미션 건너뛰기
    private void skipMission() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        call = apiService.skipMyMission(accessToken,teamId,missionId,why.getText().toString());
        call.enqueue(new Callback<MissionSkipResponse>() {
            @Override
            public void onResponse(Call<MissionSkipResponse> call, Response<MissionSkipResponse> response) {
                if(response.isSuccessful()) {
                    MissionSkipResponse skipResponse = response.body();
                    if(skipResponse.getMessage().equals("개인별 미션 건너뛰기 성공")) {
                        // 연동 성공한 경우
                        Intent intent = new Intent();
                        intent.putExtra("value", "1");
                        setResult(RESULT_OK, intent);
                        finish();

                        Log.d(TAG,"정상적으로 건너뛰기 처리 완료!");
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
                            Log.d(TAG,"만료된 토큰 입니다.");
                            ChangeJwt.updateJwtToken(MissionPassActivity.this);
                            skipMission();
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
            public void onFailure(Call<MissionSkipResponse> call, Throwable t) {
                Log.d(TAG, "미션 건너뛰기 처리 실패...");
            }
        });
    }
}