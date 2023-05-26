package com.example.moing.mypage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.moing.R;
import com.example.moing.request.ProfileUpdateRequest;
import com.example.moing.response.ProfileUpdateResponse;
import com.example.moing.response.RegisterNameResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;
import com.example.moing.s3.DownloadImageCallback;
import com.example.moing.s3.ImageUtils;
import com.example.moing.s3.S3Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageModifyActivity extends AppCompatActivity {
    private static final String TAG = "MyPageModifyActivity";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private static final int READ_EXTERNAL_STORAGE_REQUEST = 0;
    private File imageFile;
    private String uniqueFileNameWithExtension;
    private ImageView ivProfile;
    private ImageView ivGallery;
    private TextView tvNickname;
    private EditText etNickname;
    private TextView tvNicknameCnt;
    private TextView tvNicknameTf;
    private TextView tvIntroduction;
    private EditText etIntroduction;
    private TextView tvIntroductionCnt;
    private Button btnDone;
    private ImageButton btnClose;
    private String beforeProfile="";
    private String beforeNickname="", afterNickname="";
    private String beforeIntroduction="", afterIntroduction="";

    private Call<RegisterNameResponse> registerNameResponseCall;

    private Call<ProfileUpdateResponse> profileUpdateResponseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page_modify);

        // 초기 닉네임, 한줄소개, 프로필
        beforeNickname = getIntent().getStringExtra("nickname");
        if(beforeNickname == null)
            beforeNickname = "";
        beforeIntroduction = getIntent().getStringExtra("introduction");
        if(beforeIntroduction == null)
            beforeIntroduction = "";
        beforeProfile = getIntent().getStringExtra("profile");
        if(beforeProfile == null)
            beforeProfile = "";

        // 프로필
        ivProfile = findViewById(R.id.mypage_modify_iv_profile);
        // 프로필 선택 갤러리
        ivGallery = findViewById(R.id.mypage_modify_iv_gallery);
        // 닉네임 타이틀
        tvNickname = findViewById(R.id.mypage_modify_tv_nickname);
        // 닉네임 입력란
        etNickname = findViewById(R.id.mypage_modify_et_nickname);
        // 닉네임 글자수
        tvNicknameCnt = findViewById(R.id.mypage_modify_tv_nickname_cnt);
        // 닉네임 중복체크
        tvNicknameTf = findViewById(R.id.mypage_modify_tv_nickname_tf);
        // 한줄소개 타이틀
        tvIntroduction = findViewById(R.id.mypage_modify_tv_introduction);
        // 한줄소개 입력란
        etIntroduction = findViewById(R.id.mypage_modify_et_introduction);
        // 한줄소개 글자수
        tvIntroductionCnt = findViewById(R.id.mypage_modify_tv_introduction_cnt);
        // 수정 완료
        btnDone = findViewById(R.id.mypage_modify_btn_done);
        // 창 닫기
        btnClose = findViewById(R.id.mypage_modify_btn_close);

        // 프로필 초기값 설정
        S3Utils.downloadImageFromS3(beforeProfile, new DownloadImageCallback() {
            @Override
            public void onImageDownloaded(byte[] data) {
                runOnUiThread(() -> Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(data)
                        .transform(new RoundedCorners(24))
                        .into(ivProfile));
            }
            @Override
            public void onImageDownloadFailed() {
                runOnUiThread(() -> Glide.with(getApplicationContext())
                        .load(beforeProfile)
                        .into(ivProfile));
            }
        });

        // 닉네임 초기값 설정
        etNickname.setHint(beforeNickname);
        // 한줄소개 초기값 설정
        etIntroduction.setHint(beforeIntroduction);

        // 프로필 선택 갤러리 클릭 리스너 등록
        ivGallery.setOnClickListener(onGalleryBtnClickListener);

        // 닉네임 onFocusChangeListener, TextWatcher 등록
        setFocus(etNickname, tvNickname, tvNicknameCnt);
        setTextWatcher(etNickname, tvNicknameCnt, 10);

        // 한줄소개 onFocusChangeListener, TextWatcher 등록
        setFocus(etIntroduction, tvIntroduction, tvIntroductionCnt);
        setTextWatcher(etIntroduction, tvIntroductionCnt, 10);

        // 창닫기 클릭 리스너 등록
        btnClose.setOnClickListener(v -> finish());

        // 수정 완료 클릭 리스너 등록
        btnDone.setOnClickListener(onDoneBtnClickListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (registerNameResponseCall != null) {
            registerNameResponseCall.cancel(); // API 요청 취소
        }

        if (profileUpdateResponseCall != null) {
            profileUpdateResponseCall.cancel(); // API 요청 취소
        }
    }

    View.OnClickListener onDoneBtnClickListener = (v -> {
        String nickname = beforeNickname;
        String introduction = beforeIntroduction;

        if(afterNickname.length() >0 && !beforeNickname.equals(afterNickname)){
            nickname = afterNickname;
        }

        if (afterIntroduction != null &&!beforeIntroduction.equals(afterIntroduction)){
            introduction = afterIntroduction;
        }

        if(imageFile != null){
            // 이미지 업로드
            S3Utils.uploadImageToS3(getApplicationContext(),uniqueFileNameWithExtension,imageFile);
            // 소모임 수정 정보 업데이트
            putProfileUpdate(nickname,introduction,uniqueFileNameWithExtension);
        }
        else{
            putProfileUpdate(nickname,introduction,beforeProfile);
        }

        finish();
    });

    /** EditText focus 시 제목 색상 변경 **/
    private void setFocus(EditText editText, TextView textView, TextView textViewCnt) {
        editText.setOnFocusChangeListener((view, isFocused) -> {
            if(isFocused){
                textView.setTextColor(ContextCompat.getColor(this,R.color.main_dark_200));
                textViewCnt.setTextColor(ContextCompat.getColor(this,R.color.main_dark_200));
            }
            else{
                textView.setTextColor(ContextCompat.getColor(this,R.color.secondary_grey_black_7));
                textViewCnt.setTextColor(ContextCompat.getColor(this,R.color.secondary_grey_black_7));

                // 닉네임의 경우 중복 검사 시행
                if(editText.getId() == R.id.mypage_modify_et_nickname)
                    getNicknameDup();
            }
        });
    }

    /** EditText 입력 시 입력된 글자 수 변경 **/
    private void setTextWatcher(EditText editText, TextView countTextView, int maxLength) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                String result = "(" + editable.length() + "/" + maxLength + ")";
                countTextView.setText(result);

                // 닉네임 수정시 비교 후 버튼 활성화
                if(editText.getId() == R.id.mypage_modify_et_nickname){
                    afterNickname = editable.toString();
                    if(afterNickname.length() >0 && !beforeNickname.equals(editable.toString())){
                        activeBtnDone();
                    }
                    else inactiveBtnDone();
                }
                // 한줄소개 수정시 비교 후 버튼 활성화
                else if(editText.getId() == R.id.mypage_modify_et_introduction){
                    afterIntroduction = editable.toString();
                    if(afterIntroduction.length() >0 && !beforeIntroduction.equals(editable.toString())){
                        activeBtnDone();
                    }
                    else inactiveBtnDone();
                }


            }
        });
    }

    /** 버튼 클릭 시 권한 확인 후 갤러리 접근 **/
    View.OnClickListener onGalleryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // API 레벨 23 이상 READ_EXTERNAL_STORAGE 권환 필요
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // 권한 X - 요청
                    ActivityCompat.requestPermissions(MyPageModifyActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST);
                } else {
                    // 권한 O - 갤러리 접근
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(intent);
                }
            }
        }
    };


    /** 갤러리에서 이미지 선택을 위한 ActivityResultLauncher **/
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // 선택된 이미지 처리
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();

                        // Glide 를 통한 이미지 로딩
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .transform(new RoundedCorners(10))
                                .into(ivProfile);


                        // 파일의 절대경로 Get
                        String filePath = ImageUtils.getAbsolutePathFromUri(getApplicationContext(), uri);
                        if (filePath != null) {
                            // 선택한 사진을 파일로 변환
                            imageFile = new File(filePath);
                            // UUID를 사용하여 고유한 파일 이름 생성 ( 파일명.파일확장자.UUID)
                            uniqueFileNameWithExtension = ImageUtils.generateUniqueFileName(filePath);
                        }

                        activeBtnDone();
                    }
                }
            }
    );

    /** btnDone 활성화 **/
    private void activeBtnDone(){
        btnDone.setBackgroundResource(R.drawable.button_round_black1);
        btnDone.setTextColor(ContextCompat.getColorStateList(getApplicationContext(), R.color.secondary_grey_black_12));
        btnDone.setClickable(true);
    }

    /** btnDone 비활성화 **/
    private void inactiveBtnDone(){
        btnDone.setTextColor(ContextCompat.getColorStateList(getApplicationContext(), R.color.secondary_grey_black_10));
        btnDone.setBackgroundResource(R.drawable.button_round_black12);
        btnDone.setClickable(false);
    }

    /** 닉네임 중복 검사 **/
    private void getNicknameDup(){
        String nickName = etNickname.getText().toString();

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(nickName);
        registerNameResponseCall = apiService.NameAvailable(nickName);
        registerNameResponseCall.enqueue(new Callback<RegisterNameResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterNameResponse> call, @NonNull Response<RegisterNameResponse> response) {
                if (response.isSuccessful()) {
                    RegisterNameResponse registerNameResponse = response.body();
                    String result = registerNameResponse.getData().getResult();

                    if (result.equals("이미 존재하는 닉네임입니다")) {
                        // 이미 존재하는 닉네임 처리 로직
                        tvNicknameTf.setText("중복된 닉네임이에요");
                        tvNicknameTf.setVisibility(View.VISIBLE);
                        tvNicknameTf.setTextColor(Color.parseColor("#F43A6F"));
                        inactiveBtnDone();

                    } else if (result.equals("가능한 닉네임입니다")) {
                        // 가능한 닉네임 처리 로직
                        tvNicknameTf.setText("사용 가능한 닉네임이에요");
                        tvNicknameTf.setVisibility(View.VISIBLE);
                        tvNicknameTf.setTextColor(Color.parseColor("#88F0BE"));
                        activeBtnDone();
                    }
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        JSONObject errorObject = new JSONObject(errorJson);
                        // 에러 코드로 에러처리를 하고 싶을 때
                        // String errorCode = errorObject.getString("errorCode");
                        /** 메세지로 에러처리를 구분 **/
                        String message = errorObject.getString("message");

                        if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getApplicationContext());
                            getNicknameDup();
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
            public void onFailure(Call<RegisterNameResponse> call, Throwable t) {
                // 네트워크 오류 처리 로직
            }
        });
    }

    private void putProfileUpdate(String nickname, String introduction,String profile){
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        profileUpdateResponseCall = apiService.putProfileUpdate(jwtAccessToken,new ProfileUpdateRequest(introduction,nickname,profile));
        profileUpdateResponseCall.enqueue(new Callback<ProfileUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileUpdateResponse> call, @NonNull Response<ProfileUpdateResponse> response) {
                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, response.body().toString());

                    }
                }else{
                    try {
                        String errorJson = response.errorBody().string();
                        JSONObject errorObject = new JSONObject(errorJson);
                        // 에러 코드로 에러처리를 하고 싶을 때
                        // String errorCode = errorObject.getString("errorCode");
                        /** 메세지로 에러처리를 구분 **/
                        String message = errorObject.getString("message");

                        if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getApplicationContext());
                            putProfileUpdate(nickname, introduction, profile);
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
            public void onFailure(@NonNull Call<ProfileUpdateResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "실패");
            }
        });
    }
}