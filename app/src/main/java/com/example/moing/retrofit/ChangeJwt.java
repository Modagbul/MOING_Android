package com.example.moing.retrofit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.moing.request.ChangeJwtRequest;
import com.example.moing.response.ChangeJwtResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Jwt 토큰이 만료되었을 때 사용 **/
/** Context를 넣어줘야 함. 예를 들어 MainActivity 라면 MainActivity.this 를 넣어주면 됨. **/
public class ChangeJwt {
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private static final String JWT_REFRESH_TOKEN = "JWT_refresh_token";
    private static final String USER_ID = "user_id";
    public static void updateJwtToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String refresh_token = sharedPreferences.getString(JWT_REFRESH_TOKEN, null); // 액세스 토큰 검색
        Long userId = sharedPreferences.getLong(USER_ID, -1); // 액세스 토큰 검색

        RetrofitAPI ApiService = RetrofitClient.getApiService();

        /** 수정 **/
        ChangeJwtRequest request = new ChangeJwtRequest(refresh_token, userId);
        Call<ChangeJwtResponse> call = ApiService.changeJwt(request);
        call.enqueue(new Callback<ChangeJwtResponse>() {
            @Override
            public void onResponse(Call<ChangeJwtResponse> call, Response<ChangeJwtResponse> response) {
                if(response.isSuccessful()) {
                    ChangeJwtResponse jwtResponse = response.body();
                    ChangeJwtResponse.Data data = jwtResponse.getData();
                    String accessToken = data.getAccessToken();
                    String refreshToken = data.getRefreshToken();
                    /** 저장 **/
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(JWT_ACCESS_TOKEN); // 기존 값 삭제
                    editor.putString(JWT_ACCESS_TOKEN, "Bearer " + accessToken); // 수정된 값 삽입
                    editor.remove(JWT_REFRESH_TOKEN); // 기존 값 삭제
                    editor.putString(JWT_REFRESH_TOKEN, refreshToken); // 수정된 값 삽입
                    editor.apply(); // 변경사항 저장
                }
            }

            @Override
            public void onFailure(Call<ChangeJwtResponse> call, Throwable t) {
                Log.d("LOGINACTIVITY", t.getMessage());
            }
        });
    }
}
