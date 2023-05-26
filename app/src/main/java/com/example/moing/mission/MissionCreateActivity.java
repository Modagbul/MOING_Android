package com.example.moing.mission;

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

import com.example.moing.R;
import com.example.moing.request.MissionCreateRequest;
import com.example.moing.response.MissionCreateResponse;
import com.example.moing.board.BoardActivity;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionCreateActivity extends AppCompatActivity {
    private static final String TAG = "MissionCreateActivity";

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private SharedPreferences sharedPreferences;
    private Long teamId;

    Button btn_close, et_calendar, et_time, calendarIcon, create;

    EditText et_title, et_content, et_rule;

    TextView titleTv, tv_titleCount, contentTv, tv_contentCount, ruleTv, tv_ruleCount;

    ImageView xIcon;

    private  Call<MissionCreateResponse> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_create);

        // Intent 값 전달받는다.
        Intent intent = getIntent();
        teamId = intent.getLongExtra("teamId", 0);

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
        // 미션 제목 글자 수
        tv_titleCount = (TextView) findViewById(R.id.tv_titleCount);

        // 미션 마감일 - 날짜 선택 et
        et_calendar = (Button) findViewById(R.id.et_calendar);
        // 미션 마감일 - 달력 아이콘
        calendarIcon = (Button) findViewById(R.id.calendarIcon);
        // 미션 마감일 - 시간 선택 et
        et_time = (Button) findViewById(R.id.et_time);

        // 미션 내용 tv
        contentTv = (TextView) findViewById(R.id.contentTv);
        // 미션 내용 et
        et_content = (EditText) findViewById(R.id.et_content);
        // 미션 내용 글자 수
        tv_contentCount = (TextView) findViewById(R.id.tv_contentCount);

        // 인증 규칙 tv
        ruleTv = (TextView) findViewById(R.id.ruleTv);
        // 인증 규칙 et
        et_rule = (EditText) findViewById(R.id.et_rule);
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(MissionCreateActivity.this,
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
                TimePickerDialog timePickerDialog = new TimePickerDialog(MissionCreateActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // 사용자가 선택한 시간 처리
                                // hourOfDay와 minute 변수에 선택한 시간 정보가 전달됨
                                // 이곳에 선택한 시간에 대한 처리 코드를 작성하면 됩니다.
                                String hour = "";
                                if(hourOfDay < 10) {
                                    hour = "0" + String.valueOf(hourOfDay);
                                } else {
                                    hour = String.valueOf(hourOfDay);
                                }

                                String min = "";
                                if (minute < 10) {
                                    min = "0" + String.valueOf(minute);
                                } else {
                                    min = String.valueOf(minute);
                                }

                                String selectedTime = hour+":"+min;
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

        if( call != null)
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
            create.setTextColor(ContextCompat.getColorStateList(MissionCreateActivity.this, R.color.secondary_grey_black_10));
            create.setBackgroundColor(Color.parseColor("#202020"));

            // 버튼의 둥근 모서리 설정
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setCornerRadius(10); // 원하는 모서리 반지름 설정
            gradientDrawable.setColor(Color.parseColor("#202020")); // 배경 색상 설정
            create.setBackground(gradientDrawable);
        }
    }

    /** 업로드하기 버튼 **/
   View.OnClickListener uploadClickListener = v -> {
            // 미션 정보를 입력한 후 업로드하기 버튼을 클릭할 때 수행되는 코드

            // 미션 정보를 가져옴
            String title = et_title.getText().toString();
            String dueTo = et_calendar.getText().toString() + " " + et_time.getText().toString();
            String content = et_content.getText().toString();
            String rule = et_rule.getText().toString();

            uploadMission(title,dueTo,content,rule);

   };

   private void uploadMission(String title, String dueTo, String content, String rule){
       String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
       apiService = RetrofitClientJwt.getApiService(accessToken);
       // 미션 생성 요청 객체 생성
       MissionCreateRequest missionCreateRequest = new MissionCreateRequest(title, dueTo, content, rule);

       call = apiService.makeMission(accessToken, teamId, missionCreateRequest);

       call.enqueue(new Callback<MissionCreateResponse>() {
           @Override
           public void onResponse(Call<MissionCreateResponse> call, Response<MissionCreateResponse> response) {
               if (response.isSuccessful()) {
                   // 요청이 성공적으로 처리됨
                   MissionCreateResponse missionCreateResponse = response.body();
                   // 생성된 미션 데이터에 접근하여 필요한 작업 수행
                   MissionCreateResponse.MissionData missionData = missionCreateResponse.getData();

                   Intent intent = new Intent(MissionCreateActivity.this, BoardActivity.class);
                   intent.putExtra("teamId", teamId);
                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   startActivity(intent);

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
                           uploadMission(title,dueTo,content,rule);
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
           public void onFailure(Call<MissionCreateResponse> call, Throwable t) {
               // 요청이 실패함
               // 실패 처리를 위한 코드 작성
           }
       });
   }
}