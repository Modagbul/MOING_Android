package com.example.moing.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.moing.MainActivity;
import com.example.moing.R;
import com.example.moing.request.RegisterAddressRequest;
import com.example.moing.response.RegisterAddressResponse;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterInputAddressActivity extends AppCompatActivity {

    private RetrofitAPI retrofitAPI, apiService;
    private String access;
    private String nickname;

    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private static final String FCM_TOKEN = "FCM_token";
    private SharedPreferences sharedPreferences;

    private Call<RegisterAddressResponse> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_input_address);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        ImageView arrowLeft = (ImageView) findViewById(R.id.arrowLeft);
        ImageView startLight = (ImageView) findViewById(R.id.startLight);
        ImageView startDark = (ImageView) findViewById(R.id.startDark);
        EditText editText = (EditText) findViewById(R.id.editText);
        TextView regionInforTv = (TextView) findViewById(R.id.regionInforTv); // 00시 ~
        TextView regionInputTv = (TextView) findViewById(R.id.regionInputTv); // '서울시'
        TextView regionTv = (TextView) findViewById(R.id.regionTv); // 지역구 ~
        ImageView xIcon = (ImageView) findViewById(R.id.xIcon);
        TextView smallTv = (TextView) findViewById(R.id.smallTv); // 거의 ~
        TextView tv1 = (TextView) findViewById(R.id.tv1); // 주로 ~
        TextView tv2 = (TextView) findViewById(R.id.tv2); // 지역구 ~

        //retrofit 이용
        retrofitAPI = RetrofitClient.getApiService();

        // 저장해둔 토큰 찾기
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        if (accessToken != null) {
            // 액세스 토큰이 존재하는 경우의 처리 로직
            access = accessToken; // access 변수에 토큰 값 저장
            Log.d("token", "토큰: " + access + "을 찾았습니다.");
        } else {
            // 액세스 토큰이 존재하지 않는 경우의 처리 로직
            Log.d("token", "액세스 토큰 값이 저장되지 않았습니다.");
        }

        // 저장해둔 닉네임 찾기
        String nickName = sharedPreferences.getString("nickname", null); // 닉네임 검색
        if (nickName != null) {
            // 액세스 토큰이 존재하는 경우의 처리 로직
            nickname = nickName; // access 변수에 토큰 값 저장
            Log.d("nickname", "닉네임: " + nickname + "을 찾았습니다.");
        } else {
            // 액세스 토큰이 존재하지 않는 경우의 처리 로직
            Log.d("nickname", "닉네임이 저장되지 않았습니다.");
        }

        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        xIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editText.setText(null);
                startDark.setVisibility(View.VISIBLE);
                startLight.setVisibility(View.INVISIBLE);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    regionTv.setVisibility(View.VISIBLE);
                    regionInforTv.setVisibility(View.INVISIBLE);
                    regionInputTv.setVisibility(View.VISIBLE);
                    startDark.setVisibility(View.INVISIBLE);
                    startLight.setVisibility(View.VISIBLE);
                    xIcon.setVisibility(View.VISIBLE);
                    smallTv.setVisibility(View.INVISIBLE);
                    tv1.setTextColor(Color.parseColor("#66676A"));
                    tv2.setTextColor(Color.parseColor("#66676A"));

                } else {
                    smallTv.setVisibility(View.VISIBLE);
                    tv1.setTextColor(Color.parseColor("#FDFDFD"));
                    tv2.setTextColor(Color.parseColor("#FDFDFD"));

                }
            }
        });

        startLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 사용자가 입력한 주소 정보를 가져옴
                String address = editText.getText().toString();

                // fcmToken 가져오기
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String fcmToken = sharedPreferences.getString(FCM_TOKEN, null);

                // RegisterAddressRequest 객체를 생성하고 주소 정보를 담음
                String jwtAccessTokenWithoutBearer = access.replace("Bearer ", "");
                RegisterAddressRequest request = new RegisterAddressRequest(jwtAccessTokenWithoutBearer, address, nickname, fcmToken);

                // API 요청을 보내기 전에 Request Header에 토큰 값을 추가
                call = retrofitAPI.AdditionalInfo(access, request);

                // API 요청을 보내고 응답을 처리하는 콜백 메서드를 정의함
                call.enqueue(new Callback<RegisterAddressResponse>() {
                    @Override
                    public void onResponse(Call<RegisterAddressResponse> call, Response<RegisterAddressResponse> response) {
                        if (response.isSuccessful()) {
                            RegisterAddressResponse registerAddressResponse = response.body();
                            String msg = registerAddressResponse.getMessage();
                            if(msg.equals("회원 가입을 완료했습니다")) {
                                // 백에서 준 jwt
                                String jwtToken = registerAddressResponse.getData().getAccessToken();
                                Log.d("jwtToken", "jwtToken: " + jwtToken);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove(JWT_ACCESS_TOKEN);
                                editor.putString(JWT_ACCESS_TOKEN, "Bearer " + jwtToken);
                                editor.apply();

                                // 홈 화면에서 부터 요청을 jwt 토큰을 헤더에 담아서 요청??

                                Intent intent = new Intent(RegisterInputAddressActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // API 요청이 실패했을 때
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterAddressResponse> call, Throwable t) {
                        // API 요청이 실패했을 때 수행할...
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(call != null){
            call.cancel();
        }
    }
}
