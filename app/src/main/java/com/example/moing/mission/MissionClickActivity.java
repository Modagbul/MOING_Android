package com.example.moing.mission;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.moing.R;
import com.example.moing.response.MissionClearResponse;
import com.example.moing.response.MissionInfoResponse;
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

public class MissionClickActivity extends AppCompatActivity {
    private static final String TAG = "MissionClickActivity";

    private SubsamplingScaleImageView dialog_picture;
    private Dialog dialog;
    private ImageButton back_dialog, dot;
    private Button back, mission, fix, across;
    private TextView curState, d_day, title1, title2, reason, content, rule;
    private ImageView picture, smile;
    private static final int READ_EXTERNAL_STORAGE_REQUEST = 0;
    private RelativeLayout rl;
    private LinearLayout linearLayout;
    private MissionFixFragment missionFixFragment;

    private File imageFile; // 업로드할 이미지 파일
    private String uniqueFileNameWithExtension; // 업로드할 이미지 파일의 이름

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private SharedPreferences sharedPreferences;
    private Long teamId, missionId;

    private Call<MissionInfoResponse> missionInfoResponseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_click);

        // Token을 사용할 SharedPreference
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        /** Intent를 통해 teamId, missionId 받을 예정 **/
        Intent intent = getIntent();
        teamId = intent.getLongExtra("teamId", 0);
        missionId = intent.getLongExtra("missionId", 0);

        // 뒤로 가기
        back = findViewById(R.id.btn_close);
        // Dot indicator
        dot = findViewById(R.id.imgbtn_dotIndicator);
        // 인증 상태
        curState = findViewById(R.id.tv_state);
        // d_day
        d_day = findViewById(R.id.tv_dDay);
        // 맨위제목
        title1 = findViewById(R.id.tv_title);
        // 상위 레이아웃
        rl = findViewById(R.id.relative_first);
        // 레이아웃 내부 제목
        title2 = findViewById(R.id.tv_title2);
        // 스마일
        smile = findViewById(R.id.iv_smile);
        // 사유를 작성해야~ TextView
        reason = findViewById(R.id.tv_reason);
        // 사진
        picture = findViewById(R.id.iv_pic);
        // 사진 팝업 버튼
        dialog = new Dialog(MissionClickActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.mission_picture_dialog);
        // 다이얼로그에서 뒤로가기 버튼
        back_dialog = dialog.findViewById(R.id.btn_back);
        // 다이얼로그 이미지뷰 생성
        dialog_picture = dialog.findViewById(R.id.mission_pic);
        // 미션인증버튼
        mission = findViewById(R.id.btn_auth);
        // 건너뛰기 버튼
        across = findViewById(R.id.btn_across);
        // 미션 수정
        fix = findViewById(R.id.btn_fix);
        // 미션 내용
        content = findViewById(R.id.tv_mission_content);
        // 인증 규칙
        rule = findViewById(R.id.tv_mission_rule);
        // 인증 현황 보러 가기
        linearLayout = findViewById(R.id.lin_bottom);

        // 뒤로 가기 버튼 클릭 리스너
        back.setOnClickListener(backClickListener);
        // Dot indicator 클릭 리스너
        dot.setOnClickListener(dotClickListener);
        // 선택 이미지 클릭 리스너
        picture.setOnClickListener(pictureClickListener);
        // 다이얼로그 뒤로 가기 클릭 리스너
        back_dialog.setOnClickListener(dialogBackClickListener);
        // 미션 인증하기 클릭 리스너
        mission.setOnClickListener(missionClickListener);
        // 미션 수정하기 클릭 리스너
        fix.setOnClickListener(fixClickListener);
        // 인증 현황 보러 가기 클릭 리스너
        linearLayout.setOnClickListener(missionStateClickListener);

        // 미션 건너뛰기
        across.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MissionClickActivity.this, MissionPassActivity.class);
                intent.putExtra("teamId", teamId);
                intent.putExtra("missionId", missionId);
                getPassResult.launch(intent);
            }
        });

        missionInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(missionInfoResponseCall != null){
            missionInfoResponseCall.cancel();
        }
    }

    // dot Indicator 클릭 시
    View.OnClickListener dotClickListener = v -> {
        missionFixFragment = new MissionFixFragment();
        missionFixFragment.show(getSupportFragmentManager(), missionFixFragment.getTag());
    };

    // 뒤로 가기 버튼 클릭 시
    View.OnClickListener backClickListener = v -> {
        finish();
    };
    // 사진 클릭 시
    View.OnClickListener pictureClickListener = v -> {
        dialog.show();
    };
    // 다이얼로그에서 뒤로 나오기
    View.OnClickListener dialogBackClickListener = v -> {
        dialog.dismiss();
    };

    // 수정하기 버튼 클릭 시
    View.OnClickListener fixClickListener = v -> {
        gallery();
    };

    // 결과값 반환해오기
    private final ActivityResultLauncher<Intent> getPassResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 서브 액티비티로부터 돌아올 때 서브 액티비티에서 벌어지는 결과 값을 받아올 수 있는 통로..!
                if (result.getResultCode() == RESULT_OK) {
                    // 수정하지 않은 경우
                    if (result.getData().getStringExtra("value").equals("0")) {
                        changeIncomPlete();
                    } else {
                        /** 수정된 경우 **/
                        changePending();
                    }
                }
            }
    );

    /**
     * 인증 현황 보러 가기 클릭 리스너
     **/
    View.OnClickListener missionStateClickListener = v -> {
        Intent intent = new Intent(getApplicationContext(), MissionStatusActivity.class);
        /** TeamId, missionId 아직 못 받아서 Intent 받아오는 대로 연동 해놓을게 !! **/
        intent.putExtra("teamId", teamId);
        intent.putExtra("missionId", missionId);
        startActivity(intent);
    };

    /**
     * 버튼 클릭 시 권한 확인 후 갤러리 접근
     **/
    View.OnClickListener missionClickListener = v -> {
        gallery();
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
                                .into(picture);

                        // 팝업 사진에 해당 사진 업로드
                        dialog_picture.setImage(ImageSource.uri(imageFileUri));

                        // 파일의 절대경로 Get
                        String filePath = ImageUtils.getAbsolutePathFromUri(getApplicationContext(), imageFileUri);
                        if (filePath != null) {
                            // 선택한 사진을 파일로 변환
                            imageFile = new File(filePath);
                            // UUID를 사용하여 고유한 파일 이름 생성 ( 파일명.파일확장자.UUID)
                            uniqueFileNameWithExtension = ImageUtils.generateUniqueFileName(filePath);
                        }

                        missionClear();
                    }
                }
            }
    );

    /**
     * 갤러리 접근 메서드
     **/
    public void gallery() {
        // API 레벨 23 이상 READ_EXTERNAL_STORAGE 권환 필요
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 권한 X - 요청
            ActivityCompat.requestPermissions(MissionClickActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST);
        } else {
            // 권한 O - 갤러리 접근
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        }
    }

    /**
     * 미션 상세 조회 API
     **/
    public void missionInfo() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        missionInfoResponseCall = apiService.getMission(accessToken, teamId, missionId);
        missionInfoResponseCall.enqueue(new Callback<MissionInfoResponse>() {
            @Override
            public void onResponse(Call<MissionInfoResponse> call, Response<MissionInfoResponse> response) {
                MissionInfoResponse infoResponse = response.body();
                if (response.isSuccessful()) {
                    if (infoResponse.getMessage().equals("개인별 미션 상세 조회 성공")) {
                        String response_title = infoResponse.getData().getTitle();
                        String response_content = infoResponse.getData().getContent();
                        String response_dDay = infoResponse.getData().getDueTo();
                        String response_rule = infoResponse.getData().getRule();
                        String response_status = infoResponse.getData().getStatus();

                        // 값 전달
                        title1.setText(response_title);
                        title2.setText(response_title);
                        content.setText(response_content);
                        if(response_dDay != null){
                            if(Integer.parseInt(response_dDay) >= 0)
                                d_day.setText("남은 시간 D-" + response_dDay);
                            else
                                d_day.setText("인증 시간 종료");
                        }
                        rule.setText(response_rule);

                        /** 구현 예정 **/
                        if (response_status.equals("COMPLETE")) {
                            changeClear();
                        }
                        else if (response_status.equals("PENDING")) {
                            changePending();
                        }

                    }
                }
                else {
                    try {
                        /** 작성자가 아닌 경우 **/
                        String errorJson = response.errorBody().string();
                        JSONObject errorObject = new JSONObject(errorJson);
                        // 에러 코드로 에러처리를 하고 싶을 때
                        // String errorCode = errorObject.getString("errorCode");
                        /** 메세지로 에러처리를 구분 **/
                        String message = errorObject.getString("message");

                        if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(MissionClickActivity.this);
                            missionInfo();
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
            public void onFailure(Call<MissionInfoResponse> call, Throwable t) {
                Log.d(TAG, "미션 상세 조회 연동 실패 ...");
            }
        });
    }


    /**
     * 사진 인증하기 API
     **/
    public void missionClear() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        Call<MissionClearResponse> call = apiService.missionClear(accessToken, teamId, missionId, uniqueFileNameWithExtension);
        call.enqueue(new Callback<MissionClearResponse>() {
            @Override
            public void onResponse(Call<MissionClearResponse> call, Response<MissionClearResponse> response) {
                if(response.isSuccessful()) {
                    MissionClearResponse mcResponse = response.body();
                    Log.d(TAG, "통신 200 성공!!");
                    Log.d(TAG, mcResponse.getMessage());
                    // 연동 성공!
                    if(mcResponse.getMessage().equals("개인별 미션 제출 성공")) {
                        String status = mcResponse.getData();
                        // 수행완료된 경우
                        if(status.equals("COMPLETE")) {
                            // S3 사진 업로드
                            S3Utils.uploadImageToS3(getApplicationContext(), uniqueFileNameWithExtension, imageFile);
                            ColorStateList co = ColorStateList.valueOf(getResources().getColor(R.color.main_white_500));
                            rl.setBackgroundTintList(co);

                            curState.setTextColor(ContextCompat.getColor(MissionClickActivity.this, R.color.secondary_grey_black_1));
                            curState.setText("인증 완료!");
                            setVisible();
                        }
                    }
                }
                else {
                    try {
                        /** 작성자가 아닌 경우 **/
                        String errorJson = response.errorBody().string();
                        JSONObject errorObject = new JSONObject(errorJson);
                        // 에러 코드로 에러처리를 하고 싶을 때
                        // String errorCode = errorObject.getString("errorCode");
                        /** 메세지로 에러처리를 구분 **/
                        String message = errorObject.getString("message");

                        if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(MissionClickActivity.this);
                            missionClear();
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
            public void onFailure(Call<MissionClearResponse> call, Throwable t) {
                Log.d(TAG, "인증하기 S3 사진 업로드 연동 실패 ...");
            }
        });
    }

    /** 미션 인증 성공한 경우 **/
    private void changeClear() {
        ColorStateList co = ColorStateList.valueOf(getResources().getColor(R.color.main_white_500));
        rl.setBackgroundTintList(co);

        curState.setTextColor(ContextCompat.getColor(MissionClickActivity.this, R.color.secondary_grey_black_1));
        curState.setText("인증 완료!");
        setVisible();
    }

    private void changeIncomPlete() {
        ColorStateList co = ColorStateList.valueOf(getResources().getColor(R.color.secondary_grey_black_10));
        rl.setBackgroundTintList(co);

        curState.setTextColor(ContextCompat.getColor(MissionClickActivity.this, R.color.secondary_grey_black_8));
        curState.setPaintFlags(curState.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    /** 미션 건너뛰기인 경우 **/
    private void changePending() {
        ColorStateList co = ColorStateList.valueOf(getResources().getColor(R.color.sub_light_1));
        rl.setBackgroundTintList(co);

        curState.setTextColor(ContextCompat.getColor(MissionClickActivity.this, R.color.sub_dark_1));
        curState.setText("이번 미션을 건너뛰었어요");
        setVisible();
        smile.setVisibility(View.INVISIBLE);
    }

    /** 인증성공, 건너뛰기 시 가시성 설정 **/
    private void setVisible() {
        // Visibility 설정
        // visibility 설정
        picture.setVisibility(View.GONE);
        fix.setVisibility(View.GONE);
        reason.setVisibility(View.GONE);
        mission.setVisibility(View.GONE);
        across.setVisibility(View.GONE);
        smile.setVisibility(View.VISIBLE);
        title2.setPaintFlags(title2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
}