package com.example.moing.team;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import com.example.moing.request.MakeTeamRequest;
import com.example.moing.response.MakeTeamResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;
import com.example.moing.s3.ImageUtils;
import com.example.moing.s3.S3Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakeTeamActivity3 extends AppCompatActivity {
    private static final String TAG = "MakeTeamActivity3";
    private static final int READ_EXTERNAL_STORAGE_REQUEST = 0;

    private EditText etIntroduce;
    private EditText etResolution;
    private Button btnImageUpload;
    private Button btnImageUploadRe;
    private ImageView ivImageUpload;
    private Button btnCreateTeam;
    private ImageView ivProgressBar;

    //Intent Data
    private String major, name, cnt, date, predictDate;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";

    private File imageFile;  // 업로드할 이미지 파일
    private String uniqueFileNameWithExtension; // 업로드할 이미지 파일의 이름

    private Call<MakeTeamResponse> call;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_team3);

        // Intent 를 통한 이전 값 전달 받기
        Intent intent = getIntent();
        name = intent.getStringExtra("name"); // 소모임 이름
        cnt = intent.getStringExtra("member"); // 소모임 구성원 수
        date = intent.getStringExtra("startDate"); // 소모임 시작일
        predictDate = intent.getStringExtra("predict"); // 소모임 예상 활동 기간
        major = intent.getStringExtra("major"); // 소모임 목표

        /* API 통신을 위한 헤더에 담을 토큰값 가져오기 **/
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 뒤로 가기
        ImageButton btnBack = findViewById(R.id.make_team3_btn_back);
        btnBack.setOnClickListener(view -> finish());

        // 소모임 소개
        etIntroduce = findViewById(R.id.make_team3_et_introduce);
        TextView tvIntroduce = findViewById(R.id.make_team3_tv_introduce);
        TextView tvIntroduceCnt = findViewById(R.id.make_team3_tv_introduce_count);

        // 소모임장 각오
        etResolution = findViewById(R.id.make_team3_et_resolution);
        TextView tvResolution = findViewById(R.id.make_team3_tv_resolution);
        TextView tvResolutionCnt = findViewById(R.id.make_team3_tv_resolution_count);

        // 소모임 생성 및 진행 상황
        btnCreateTeam = findViewById(R.id.make_team3_btn_create_team);
        ivProgressBar = findViewById(R.id.make_team3_iv_progress);

        // 소모임 사진 업로드
        btnImageUpload = findViewById(R.id.make_team3_btn_image_upload);
        btnImageUploadRe = findViewById(R.id.make_team3_btn_image_upload_re);
        ivImageUpload = findViewById(R.id.make_team3_iv_image_upload);

        // onFocusChangeListener, TextWatcher 등록
        setFocus(etIntroduce, tvIntroduce, tvIntroduceCnt);
        setTextWatcher(etIntroduce, tvIntroduceCnt, 300);
        setFocus(etResolution, tvResolution, tvResolutionCnt);
        setTextWatcher(etResolution, tvResolutionCnt, 100);

        // onClickListener 등록
        btnImageUpload.setOnClickListener(onImageUploadClickListener);
        btnImageUploadRe.setOnClickListener(onImageUploadClickListener);
        btnCreateTeam.setOnClickListener(onCreateTeamClickListener);

        // 모임 생성 버튼 비활성화
        btnCreateTeam.setClickable(false);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel(); // API 요청 취소
        }
    }

    /**
     * 버튼 클릭 시 소모임 Data 전송 후 다음 화면 실행
     **/
    View.OnClickListener onCreateTeamClickListener = view -> {
        // 구현 예정 - 소모임 Data Retrofit 사용하여 전달

        String period = predictDate.substring(0, 1);
        String category = "";

        switch (major) {
            case "스포츠/운동":
                category = "SPORTS";
                break;
            case "생활습관 개선":
                category = "HABIT";
                break;
            case "시험/취업준비":
                category = "TEST";
                break;
            case "스터디/공부":
                category = "STUDY";
                break;
            case "독서":
                category = "READING";
                break;
            case "그외 자기계발":
                category = "ETC";
                break;
        }

        postMakeTeam(category, period);
    };

    private void postMakeTeam(String category, String period) {
        String token = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, token);
        RetrofitAPI apiService = RetrofitClientJwt.getApiService(token);

        MakeTeamRequest makeTeamRequest = new MakeTeamRequest(category, etIntroduce.getText().toString(), name,
                Integer.parseInt(period), Integer.parseInt(cnt), uniqueFileNameWithExtension,
                etResolution.getText().toString(), date);

        call = apiService.makeTeam(token, makeTeamRequest);
        call.enqueue(new Callback<MakeTeamResponse>() {
            @Override
            public void onResponse(@NonNull Call<MakeTeamResponse> call, @NonNull Response<MakeTeamResponse> response) {
                // 응답 성공 시
                Log.d(TAG, "응답 성공");

                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // s3를 통한 사진 업로드
                        S3Utils.uploadImageToS3(getApplicationContext(), uniqueFileNameWithExtension, imageFile);

                        // 다음 화면
                        Intent intent = new Intent(getApplicationContext(), MakeTeamActivity4.class);
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

                        if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getApplicationContext());
                            postMakeTeam(category, period);
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
            public void onFailure(@NonNull Call<MakeTeamResponse> call, @NonNull Throwable t) {
                // 응답 실패 시
                Log.d("MAKETEAMACTIVITY3", "응답 실패 ...");
            }
        });
    }

    /**
     * 버튼 클릭 시 권한 확인 후 갤러리 접근
     **/
    View.OnClickListener onImageUploadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // API 레벨 23 이상 READ_EXTERNAL_STORAGE 권환 필요
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 권한 X - 요청
                ActivityCompat.requestPermissions(MakeTeamActivity3.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST);
            } else {
                // 권한 O - 갤러리 접근
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(intent);
            }
        }
    };

    /**
     * 갤러리에서 이미지 선택을 위한 ActivityResultLauncher
     **/
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // 선택된 이미지 처리
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // 업로드할 이미지 파일의 uri
                        Uri imageFileUri = result.getData().getData();

                        // Glide 를 통한 이미지 로딩
                        Glide.with(getApplicationContext())
                                .load(imageFileUri)
                                .transform(new RoundedCorners(10))
                                .into(ivImageUpload);

                        // visibility 설정
                        btnImageUpload.setVisibility(View.GONE);
                        ivImageUpload.setVisibility(View.VISIBLE);
                        btnImageUploadRe.setVisibility(View.VISIBLE);

                        checkInputs();

                        // 파일의 절대경로 Get
                        String filePath = ImageUtils.getAbsolutePathFromUri(getApplicationContext(), imageFileUri);
                        if (filePath != null) {
                            // 선택한 사진을 파일로 변환
                            imageFile = new File(filePath);
                            // UUID를 사용하여 고유한 파일 이름 생성 ( 파일명.파일확장자.UUID)
                            uniqueFileNameWithExtension = ImageUtils.generateUniqueFileName(filePath);
                        }
                    }
                }
            }
    );

    /**
     * EditText focus 시 제목 색상 변경
     **/
    private void setFocus(EditText editText, TextView textView, TextView textViewCnt) {
        editText.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.main_dark_200));
                textViewCnt.setTextColor(ContextCompat.getColor(this, R.color.main_dark_200));
            } else {
                textView.setTextColor(ContextCompat.getColor(this, R.color.secondary_grey_black_7));
                textViewCnt.setTextColor(ContextCompat.getColor(this, R.color.secondary_grey_black_7));
            }
        });
    }

    /**
     * EditText 입력 시 입력된 글자 수 변경
     **/
    private void setTextWatcher(EditText editText, TextView countTextView, int maxLength) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String result = "(" + editable.length() + "/" + maxLength + ")";
                countTextView.setText(result);
                checkInputs();
            }
        });
    }

    /**
     * 소모임 소개, 각오, 사진이 올바르게 입력되었는지 확인 후 버튼 및 ProgressBar 변경
     **/
    private void checkInputs() {
        if (etIntroduce.length() > 0 && etResolution.length() > 0 && ivImageUpload.getVisibility() == View.VISIBLE) {
            btnCreateTeam.setBackgroundResource(R.drawable.button_round_black1);
            btnCreateTeam.setTextColor(ContextCompat.getColorStateList(getApplicationContext(), R.color.secondary_grey_black_12));
            btnCreateTeam.setClickable(true);
            ivProgressBar.setBackgroundResource(R.drawable.maketeam_progress5);
        } else {
            btnCreateTeam.setTextColor(ContextCompat.getColorStateList(getApplicationContext(), R.color.secondary_grey_black_10));
            btnCreateTeam.setBackgroundResource(R.drawable.button_round_black12);
            btnCreateTeam.setClickable(false);
            ivProgressBar.setBackgroundResource(R.drawable.maketeam_progress4);
        }
    }


}