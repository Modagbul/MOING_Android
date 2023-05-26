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

import com.example.moing.R;
import com.example.moing.response.RegisterNameResponse;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterInputNameActivity extends AppCompatActivity {

    private RetrofitAPI retrofitAPI;
    private String access;

    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";

    private SharedPreferences sharedPreferences;

    private Call<RegisterNameResponse> call;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_input_name);

        ImageView arrowLeft = (ImageView) findViewById(R.id.arrowLeft);
        ImageView nextLight = (ImageView) findViewById(R.id.nextLight);
        ImageView nextDark = (ImageView) findViewById(R.id.nextDark);
        ImageView nicknameBtn = (ImageView) findViewById(R.id.nicknameBtn);
        ImageView xIcon = (ImageView) findViewById(R.id.xIcon);
        EditText editText = (EditText) findViewById(R.id.editText);
        TextView nicknameTv = (TextView) findViewById(R.id.nicknameTv);
        TextView nickNameTF = (TextView) findViewById(R.id.nickNameTF);
        TextView smallTv = (TextView) findViewById(R.id.smallTv); // 반가워요 ~
        TextView tv1 = (TextView) findViewById(R.id.tv1); // MOING에서 ~
        TextView tv2 = (TextView) findViewById(R.id.tv2); // 닉네임을 ~

        //retrofit 이용
        retrofitAPI = RetrofitClient.getApiService();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        if (accessToken != null) {
            // 액세스 토큰이 존재하는 경우의 처리 로직
            access = accessToken; // access 변수에 토큰 값 저장
            System.out.println("토큰: " + access + "을 찾았습니다.");
        } else {
            // 액세스 토큰이 존재하지 않는 경우의 처리 로직
            System.out.println("액세스 토큰 값이 저장되지 않았습니다.");
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
                nextDark.setVisibility(View.VISIBLE);
                nextLight.setVisibility(View.INVISIBLE);
                nicknameBtn.setVisibility(View.INVISIBLE);
                nicknameTv.setVisibility(View.INVISIBLE);

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
                    nicknameTv.setVisibility(View.VISIBLE);
                    nextDark.setVisibility(View.INVISIBLE);
                    nicknameBtn.setVisibility(View.VISIBLE);
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

        nicknameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nickname = editText.getText().toString();

                call = retrofitAPI.NameAvailable(nickname);
                call.enqueue(new Callback<RegisterNameResponse>() {
                    @Override
                    public void onResponse(Call<RegisterNameResponse> call, Response<RegisterNameResponse> response) {
                        if (response.isSuccessful()) {
                            RegisterNameResponse registerNameResponse = response.body();
                            String result = registerNameResponse.getData().getResult();

                            if (result.equals("이미 존재하는 닉네임입니다")) {
                                // 이미 존재하는 닉네임 처리 로직
                                nickNameTF.setText("이미 존재하는 닉네임입니다.");
                                nickNameTF.setVisibility(View.VISIBLE);

                            } else if (result.equals("가능한 닉네임입니다")) {

                                // 가능한 닉네임 처리 로직
                                nickNameTF.setVisibility(View.VISIBLE);
                                nextDark.setVisibility(View.INVISIBLE);

                                // 닉네임을 SharedPreferences에 저장
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("nickname", nickname);
                                Log.d("nickname", "nickname: " + nickname + "이 저장되었습니다.");
                                editor.apply();

                            }
                        } else {
                            // 응답 실패 처리 로직
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterNameResponse> call, Throwable t) {
                        // 네트워크 오류 처리 로직
                    }
                });

                nicknameBtn.setVisibility(View.INVISIBLE);
                nextLight.setVisibility(View.VISIBLE);
                nicknameTv.setVisibility(View.INVISIBLE);
                smallTv.setVisibility(View.VISIBLE);
                tv1.setTextColor(Color.parseColor("#FDFDFD"));
                tv2.setTextColor(Color.parseColor("#FDFDFD"));
            }
        });

        // click next in
        nextLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RegisterInputNameActivity.this, RegisterInputAddressActivity.class);
                startActivity(intent);

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