package com.example.moing.board;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

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
import com.example.moing.request.TeamUpdateRequest;
import com.example.moing.response.TeamUpdateResponse;
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
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardFixTeamActivity extends AppCompatActivity {
    private static final String TAG = "BoardFixTeamActivity";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private static final int READ_EXTERNAL_STORAGE_REQUEST = 0;

    private long teamId;

    // 소모임 이름
    private String beforeName, afterName="";

    // 종료 날짜
    private Button btnEndDate;
    private String beforeEndDate, afterEndDate;

    // 소모임 이미지 업로드
    private Button btnImageUploadRe;
    private ImageView ivImageUpload;


    private File imageFile;
    private String uniqueFileNameWithExtension;
    private String beforeImage;

    // 소모임 정보 수정
    private Button btnFixTeam;

    private Call<TeamUpdateResponse> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_fix_team);


        // 구현 예정 - 서버 통신으로 소모임 정보 받아오기
        // 기존 소모임 이름, 종료 날짜, 사진
        teamId = getIntent().getLongExtra("teamId",0);
        beforeName = getIntent().getStringExtra("name");
        beforeEndDate = getIntent().getStringExtra("endDate");
        beforeImage = getIntent().getStringExtra("profileImg");
        Log.d(TAG,beforeName+" "+beforeEndDate+" "+beforeImage);

        // 소모임 이름
        EditText etName = findViewById(R.id.board_fix_et_name);
        TextView tvName = findViewById(R.id.board_fix_tv_name);
        TextView tvNameCnt = findViewById(R.id.board_fix_tv_name_count);
        // onFocusChangeListener, TextWatcher 등록
        setFocus(etName, tvName, tvNameCnt);
        setTextWatcher(etName, tvNameCnt, 10);
        // 초기값 설정
        etName.setHint(beforeName);

        // 소모임 종료 날짜
        TextView tvEndDate = findViewById(R.id.board_fix_tv_end_date);
        btnEndDate = findViewById(R.id.board_fix_btn_end_date);
        // onClickListener 등록
        btnEndDate.setOnClickListener(onEndDateClickListener);
        // 초기값 설정
        btnEndDate.setHint(beforeEndDate);

        // 소모임 사진 업로드
        btnImageUploadRe = findViewById(R.id.board_fix_btn_image_upload_re);
        ivImageUpload = findViewById(R.id.board_fix_iv_image_upload);
        // onClickListener 등록
        ivImageUpload.setOnClickListener(onImageUploadClickListener);
        btnImageUploadRe.setOnClickListener(onImageUploadClickListener);

        // 초기값 설정
        S3Utils.downloadImageFromS3(beforeImage, new DownloadImageCallback() {
            @Override
            public void onImageDownloaded(byte[] data) {
                runOnUiThread(() -> Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(data)
                        .transform(new RoundedCorners(24))
                        .into(ivImageUpload));
            }
            @Override
            public void onImageDownloadFailed() {

            }
        });

        // 소모임 정보 수정
        btnFixTeam = findViewById(R.id.board_fix_btn_fix_team);
        // onClickListener 등록
        btnFixTeam.setOnClickListener(onFixTeamClickListener);
        btnFixTeam.setClickable(false);

        // 닫기
        ImageButton btnClose = findViewById(R.id.board_fix_btn_close);
        btnClose.setOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(call != null)
            call.cancel();
    }

    /** Button click 시 변경된 소모임 이름 or 종료날짜 or 사진 서버에 전송 구현 예정) 및 종료 **/
    View.OnClickListener onFixTeamClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String name = beforeName;
            String endDate = beforeEndDate;

            if(afterName.length() >0 && !beforeName.equals(afterName)){
                name = afterName;
            }

            if (afterEndDate != null &&!beforeEndDate.equals(afterEndDate)){
                endDate = afterEndDate;
            }
            Log.d(TAG,endDate);

            if(imageFile != null){
                // 이미지 업로드
                S3Utils.uploadImageToS3(getApplicationContext(),uniqueFileNameWithExtension,imageFile);
                // 소모임 수정 정보 업데이트
                putTeamUpdate(name,endDate,uniqueFileNameWithExtension);
            }
            else{
                putTeamUpdate(name,endDate,beforeImage);
            }

            finish();
        }
    };

    /** Button click 시 소모임 종료 날짜 설정 **/
    View.OnClickListener onEndDateClickListener = view -> {
        Calendar calendar = Calendar.getInstance();
        int initialYear = calendar.get(Calendar.YEAR); // 년
        int initialMonth = calendar.get(Calendar.MONTH); // 월
        int initialDay = calendar.get(Calendar.DAY_OF_MONTH); // 일

        // DatePickerDialog를 통해 날짜 선택
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePickerView, year, month, dayOfMonth) -> {
            // 선택한 날짜
            String monthFormatted = String.format(Locale.getDefault(), "%02d", (month + 1));
            afterEndDate = year + "-" + monthFormatted + "-" + dayOfMonth;

            // 선택한 날짜와 기존 값이 다른 경우에만 버튼을 활성화
            if (!beforeEndDate.equals(afterEndDate)) {
                btnEndDate.setHint(afterEndDate);
                activeBtnFixTeam();
            } else {
                btnEndDate.setHint(beforeEndDate);
                inactiveBtnFixTeam();
            }

        }, initialYear, initialMonth, initialDay);
        datePickerDialog.show();
    };

    /** 버튼 클릭 시 권한 확인 후 갤러리 접근 **/
    View.OnClickListener onImageUploadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // API 레벨 23 이상 READ_EXTERNAL_STORAGE 권환 필요
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // 권한 X - 요청
                    ActivityCompat.requestPermissions(BoardFixTeamActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST);
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
                                .into(ivImageUpload);

                        // visibility 설정
                        btnImageUploadRe.setVisibility(View.VISIBLE);

                        // 파일의 절대경로 Get
                        String filePath = ImageUtils.getAbsolutePathFromUri(getApplicationContext(), uri);
                        if (filePath != null) {
                            // 선택한 사진을 파일로 변환
                            imageFile = new File(filePath);
                            // UUID를 사용하여 고유한 파일 이름 생성 ( 파일명.파일확장자.UUID)
                            uniqueFileNameWithExtension = ImageUtils.generateUniqueFileName(filePath);
                        }

                        activeBtnFixTeam();
                    }
                }
            }
    );

    /** EditText focus 시 제목 색상 변경 **/
    private void setFocus(EditText editText, TextView textView, TextView textViewCnt) {
        editText.setOnFocusChangeListener((view, isFocused) -> {
            if(isFocused){
                textView.setTextColor(getResources().getColor(R.color.main_dark_200));
                textViewCnt.setTextColor(getResources().getColor(R.color.main_dark_200));
            }
            else{
                textView.setTextColor(getResources().getColor(R.color.secondary_grey_black_7));
                textViewCnt.setTextColor(getResources().getColor(R.color.secondary_grey_black_7));
            }
        });
    }

    /** EditText 입력 시 입력된 글자 수 변경 **/
    private void setTextWatcher(EditText editText, TextView countTextView, int maxLength) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String result = "(" + editable.length() + "/" + maxLength + ")";
                countTextView.setText(result);
                afterName = editable.toString();
                if(afterName.length() >0 && !beforeName.equals(editable.toString())){
                    activeBtnFixTeam();
                }
                else inactiveBtnFixTeam();
            }
        });
    }

    /** btnFixTeam 활성화 **/
    private void activeBtnFixTeam(){
        btnFixTeam.setBackgroundResource(R.drawable.button_round_black1);
        btnFixTeam.setTextColor(ContextCompat.getColorStateList(getApplicationContext(), R.color.secondary_grey_black_12));
        btnFixTeam.setClickable(true);
    }

    /** btnFixTeam 비활성화 **/
    private void inactiveBtnFixTeam(){
        btnFixTeam.setTextColor(ContextCompat.getColorStateList(getApplicationContext(), R.color.secondary_grey_black_10));
        btnFixTeam.setBackgroundResource(R.drawable.button_round_black12);
        btnFixTeam.setClickable(false);
    }

    /**
     * 소모임 정보 수정
     **/
    private void putTeamUpdate(String name, String endDate, String image) {
        Log.d(TAG,endDate);
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        call = apiService.putTeamUpdate(jwtAccessToken, teamId, new TeamUpdateRequest(endDate, name, image, teamId));
        call.enqueue(new Callback<TeamUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamUpdateResponse> call, @NonNull Response<TeamUpdateResponse> response) {
                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, response.body().toString());

                    }
                }else{
                    try {
                        /** 작성자가 아닌 경우 **/
                        String errorJson = response.errorBody().string();
                        JSONObject errorObject = new JSONObject(errorJson);
                        // 에러 코드로 에러처리를 하고 싶을 때
                        // String errorCode = errorObject.getString("errorCode");
                        /** 메세지로 에러처리를 구분 **/
                        String message = errorObject.getString("message");

                        if(message.equals("소모임장이 아니어서 할 수 없습니다.")) {
                            Toast.makeText(getApplicationContext(), "소모임장이 아니어서 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getApplicationContext());
                            putTeamUpdate(name, endDate, image);
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
            public void onFailure(@NonNull Call<TeamUpdateResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "실패");
            }
        });
    }
}