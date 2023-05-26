package com.example.moing.mission;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.moing.R;
import com.example.moing.request.MissionUpdateRequest;
import com.example.moing.response.MissionUpdateResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;

import java.io.IOException;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionReviseActivity extends AppCompatActivity {

    private static final String TAG = "MissionReviseActivity";
    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private SharedPreferences sharedPreferences;
    private Long teamId, missionId;

    private String beforeTitle, afterTitle="";
    private String beforeDueTo, afterDueTo;
    private String beforeContent, afterContent="";
    private String beforeRule, afterRule="";
    private String beforeStatus, afterStatus="";

    Button btn_close, et_calendar, et_time, calendarIcon, create;

    EditText et_title, et_content, et_rule;

    TextView titleTv, tv_titleCount, contentTv, tv_contentCount, ruleTv, tv_ruleCount;

    ImageView xIcon;

    private Call<MissionUpdateResponse> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_revise);

        // Intent 값 전달받는다.
        Intent intent = getIntent();
        teamId = intent.getLongExtra("teamId", 0);
        missionId = getIntent().getLongExtra("missionId",0);
        beforeTitle = getIntent().getStringExtra("title");
        // beforeDueTo = getIntent().getStringExtra("dueTo");
        beforeContent = getIntent().getStringExtra("content");
        beforeRule = getIntent().getStringExtra("rule");
        beforeStatus = getIntent().getStringExtra("status");
        Log.d(TAG,beforeTitle+" "+beforeDueTo+" "+beforeContent+" "+beforeRule+" "+beforeStatus);

        // Token을 사용할 SharedPreference
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 미션 만들기 취소 버튼 & 클릭 리스너
        btn_close = (Button) findViewById(R.id.btn_close);
        btn_close.setOnClickListener(closeClickListener);

        // 생성 버튼
        create = (Button) findViewById(R.id.btn_create);
        create.setOnClickListener(uploadClickListener);
        create.setClickable(false);

        // 미션 제목 tv
        titleTv = (TextView) findViewById(R.id.titleTv);
        // 미션 제목 et
        et_title = (EditText) findViewById(R.id.et_title);
        et_title.setHint(beforeTitle);
        // 미션 제목 글자 수
        tv_titleCount = (TextView) findViewById(R.id.tv_titleCount);

        // 미션 마감일 - 날짜 선택 et
        et_calendar = (Button) findViewById(R.id.et_calendar);
        et_calendar.setHint(beforeDueTo);
        // 미션 마감일 - 달력 아이콘
        calendarIcon = (Button) findViewById(R.id.calendarIcon);
        // 미션 마감일 - 시간 선택 et
        et_time = (Button) findViewById(R.id.et_time);

        // 미션 내용 tv
        contentTv = (TextView) findViewById(R.id.contentTv);
        // 미션 내용 et
        et_content = (EditText) findViewById(R.id.et_content);
        et_content.setHint(beforeContent);
        // 미션 내용 글자 수
        tv_contentCount = (TextView) findViewById(R.id.tv_contentCount);

        // 인증 규칙 tv
        ruleTv = (TextView) findViewById(R.id.ruleTv);
        // 인증 규칙 et
        et_rule = (EditText) findViewById(R.id.et_rule);
        et_rule.setHint(beforeRule);
        // 인증 규칙 글자 수
        tv_ruleCount = (TextView) findViewById(R.id.tv_ruleCount);

        // 제목 입력 삭제
        xIcon = (ImageView) findViewById(R.id.xIcon);

        xIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                et_title.setText(null);

            }
        });

        // TextWatcher 등록
        setTextWatcher(et_title, tv_titleCount, 15);
        setTextWatcher2(et_content, tv_contentCount, 300);
        setTextWatcher3(et_rule, tv_ruleCount, 300);


        // 날짜 설정
        et_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 날짜 가져오기
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // DatePickerDialog 생성
                DatePickerDialog datePickerDialog = new DatePickerDialog(MissionReviseActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // 사용자가 선택한 날짜 처리
                                // year, monthOfYear, dayOfMonth 변수에 선택한 날짜 정보가 전달됨

                                // 월과 일이 한 자리 수인 경우 앞에 0을 붙여줍니다.
                                String monthString = (monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : String.valueOf(monthOfYear + 1);
                                String dayString = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);

                                String selectedDate = year + "-" + monthString + "-" + dayString;
                                et_calendar.setText(selectedDate);
                            }
                        }, year, month, day);

                // minDate 설정 (오늘 이전의 날짜는 선택하지 못하도록 함)
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

                // DatePickerDialog 표시
                datePickerDialog.show();
            }
        });

        // 시간 설정
        et_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 시간 가져오기
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // TimePickerDialog 생성
                TimePickerDialog timePickerDialog = new TimePickerDialog(MissionReviseActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // 사용자가 선택한 시간 처리
                                // hourOfDay와 minute 변수에 선택한 시간 정보가 전달됨
                                // 이곳에 선택한 시간에 대한 처리 코드를 작성하면 됩니다.
                                String selectedTime = hourOfDay + ":" + minute;
                                et_time.setText(selectedTime);
                            }
                        }, hour, minute, false);

                // TimePickerDialog 표시
                timePickerDialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(call != null)
            call.cancel();
    }

    // 취소 버튼
    View.OnClickListener closeClickListener = v -> {
        finish();
    };

    /** EditText 입력 시 입력된 글자 수 변경 **/
    private void setTextWatcher(EditText editText, TextView countTextView, int maxLength) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.length() > 0) {
                    String result = "(" + editable.length() + "/" + maxLength + ")";
                    countTextView.setText(result);
                    checkInputs();

                    xIcon.setVisibility(View.VISIBLE);
                    titleTv.setTextColor(Color.parseColor("#FFBEB5"));
//                    contentTv.setTextColor(Color.parseColor("#FFBEB5"));
//                    ruleTv.setTextColor(Color.parseColor("#FFBEB5"));

                } else {

                    xIcon.setVisibility(View.INVISIBLE);
                    titleTv.setTextColor(Color.parseColor("#959698"));
//                    contentTv.setTextColor(Color.parseColor("#959698"));
//                    ruleTv.setTextColor(Color.parseColor("#959698"));

                }
            }
        });
    }

    private void setTextWatcher2(EditText editText, TextView countTextView, int maxLength) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.length() > 0) {
                    String result = "(" + editable.length() + "/" + maxLength + ")";
                    countTextView.setText(result);
                    checkInputs();

                    contentTv.setTextColor(Color.parseColor("#FFBEB5"));

                } else {

                    contentTv.setTextColor(Color.parseColor("#959698"));

                }
            }
        });
    }

    private void setTextWatcher3(EditText editText, TextView countTextView, int maxLength) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.length() > 0) {
                    String result = "(" + editable.length() + "/" + maxLength + ")";
                    countTextView.setText(result);
                    checkInputs();

                    ruleTv.setTextColor(Color.parseColor("#FFBEB5"));

                } else {
                    ruleTv.setTextColor(Color.parseColor("#959698"));

                }
            }
        });
    }

    // 각 EditText들의 null값 여부 확인
    public boolean isEditTextFilled() {
        if (et_title.getText().toString().isEmpty() || et_content.getText().toString().isEmpty() || et_rule.getText().toString().isEmpty()) {
            return false;
        }

        return true;
    }

    // 입력값 확인
    public void checkInputs() {
        if (isEditTextFilled() ) {
            create.setClickable(true);
            create.setTextColor(Color.parseColor("#202020"));
            create.setBackgroundColor(Color.parseColor("#FFFFFF"));

            // 버튼의 둥근 모서리 설정
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setCornerRadius(10); // 원하는 모서리 반지름 설정
            gradientDrawable.setColor(Color.parseColor("#FFFFFF")); // 배경 색상 설정
            create.setBackground(gradientDrawable);

        }

        // upload버튼 비활성
        else {
            create.setClickable(false);
            create.setTextColor(ContextCompat.getColorStateList(MissionReviseActivity.this, R.color.secondary_grey_black_10));
            create.setBackgroundColor(Color.parseColor("#202020"));

            // 버튼의 둥근 모서리 설정
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setCornerRadius(10); // 원하는 모서리 반지름 설정
            gradientDrawable.setColor(Color.parseColor("#202020")); // 배경 색상 설정
            create.setBackground(gradientDrawable);
        }
    }

    /** Button click 시 변경된 미션 이름 or 날짜/시간 or 미션 내용 or 인증 규칙) 및 종료 **/
    View.OnClickListener uploadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String title = beforeTitle;
            String dueto = et_calendar.getText().toString() + " " + et_time.getText().toString();
            String content = beforeContent;
            String rule = beforeRule;

            if(afterTitle.length() >0 && !beforeTitle.equals(afterTitle)){
                title = afterTitle;
                Log.d(TAG, "바뀐 title : " + title);
            }

            if (afterDueTo != null &&!beforeDueTo.equals(afterDueTo)){
                dueto = afterDueTo;
                Log.d(TAG, "바뀐 dueTO : " + dueto);
            }
            Log.d(TAG,dueto);

            if(afterContent.length() >0 && !beforeContent.equals(afterContent)){
                content = afterContent;
                Log.d(TAG, "바뀐 content : " + content);
            }

            if(afterRule.length() >0 && !beforeRule.equals(afterRule)){
                rule = afterRule;
                Log.d(TAG, "바뀐 rule : " + rule);
            }

            else{
                putMissionUpdate(title,dueto,content,rule);
            }

            finish();

        }
    };

    /**
     * 미션 정보 수정
     **/
    private void putMissionUpdate(String title, String dueTo, String content, String rule) {
//        Log.d(TAG,endDate);
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        call = apiService.putMissionUpdate(jwtAccessToken, teamId, missionId, new MissionUpdateRequest(title, dueTo, content, rule));
        call.enqueue(new Callback<MissionUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<MissionUpdateResponse> call, @NonNull Response<MissionUpdateResponse> response) {

                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, response.body().toString());

                        Log.d(TAG, "Title: " + title);
                        Log.d(TAG, "DueTo: " + dueTo);
                        Log.d(TAG, "Content: " + content);
                        Log.d(TAG, "Rule: " + rule);
                    }
                }
                else{
                    try {
                        Log.d(TAG, response.errorBody().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    switch (response.message()) {

                        case "만료된 토큰입니다.":
                            ChangeJwt.updateJwtToken(getApplicationContext());
                            putMissionUpdate(title, dueTo, content, rule);
                            break;
                        case "소모임장이 아니어서 할 수 없습니다.":
                            Toast.makeText(getApplicationContext(), "소모임장이 아니어서 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MissionUpdateResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "실패");
            }
        });
    }
}