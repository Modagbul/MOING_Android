package com.example.moing.board.vote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.moing.R;
import com.example.moing.request.BoardMakeVoteRequest;
import com.example.moing.response.BoardMakeVoteResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardMakeVote extends AppCompatActivity implements MakeVoteAdapter.OnEditTextChangedListener {
    private static final String TAG = "BoardMakeVote";

    RecyclerView recyclerView;
    Button btn_close, btn_erase_content, addContent, deleteContent, upload;
    CheckBox  anony, multi;
    EditText title, content;
    TextView titleCount, contentCount, tv_anony, tv_multi;
    List<MakeVote> makeVoteList = new ArrayList<>();
    List<MakeVote> deleteMakeVote = new ArrayList<>();
    private int addContentCount;
    private MakeVoteAdapter makeVoteAdapter;
    private Long teamId;

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private SharedPreferences sharedPreferences;

    private Call<BoardMakeVoteResponse> call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_make_vote);

        // Intent 값 전달 받기
        Intent intent = getIntent();
        teamId = intent.getLongExtra("teamId", 0);

        // Token을 사용할 SharedPreference
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 투표 만들기 취소 버튼 & 클릭 리스너
        btn_close = (Button) findViewById(R.id.btn_close);
        btn_close.setOnClickListener(closeClickListener);
        // 제목
        title = (EditText) findViewById(R.id.et_title);
        // 제목 글자 수
        titleCount = (TextView) findViewById(R.id.tv_titleCount);
        // 내용
        content = (EditText) findViewById(R.id.et_content);
        // 내용 글자 수
        contentCount = (TextView) findViewById(R.id.tv_contentCount);

        // 예정 : 항목지우기 클릭 리스너
        btn_erase_content = (Button) findViewById(R.id.btn_erase);
        btn_erase_content.setOnClickListener(contentEraseClickListener);
        btn_erase_content.setClickable(false);

        // 투표 항목 추가하기 & 리스너
        addContent = (Button) findViewById(R.id.imgbtn_add);
        addContent.setOnClickListener(addContentClickListener);
        addContentCount=0;

        // 선택한 항목 지우기
        deleteContent = (Button) findViewById(R.id.imgbtn_erase);
        deleteContent.setOnClickListener(deleteContentClickListener);

        // TextWatcher 등록
        setTextWatcher(title, titleCount, 15);
        setTextWatcher(content, contentCount, 300);

        recyclerView = findViewById(R.id.recycle_vote);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        // 리싸이클러뷰 스크롤 방지
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.setAutoMeasureEnabled(true);

        // 리싸이클러뷰 어댑터 설정
        // adapter 설정
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        makeVoteAdapter = new MakeVoteAdapter(makeVoteList, this, this);
        recyclerView.setAdapter(makeVoteAdapter);

        // 익명, 복수 투표
        anony = (CheckBox) findViewById(R.id.btn_anony);
        multi = (CheckBox) findViewById(R.id.btn_multi);
        tv_anony = (TextView) findViewById(R.id.tv_anony);
        tv_multi = (TextView) findViewById(R.id.tv_multi);
        anony.setOnClickListener(anonyClickListener);
        multi.setOnClickListener(multiClickListener);

        // 업로드 버튼
        upload = (Button) findViewById(R.id.btn_upload);
        upload.setOnClickListener(uploadClickListener);
        upload.setClickable(false);
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
                String result = "(" + editable.length() + "/" + maxLength + ")";
                countTextView.setText(result);
                checkInputs();
            }
        });
    }

    /** 항목 추가하기 버튼 클릭 **/
    View.OnClickListener addContentClickListener = v -> {
        Log.d("CONTENTCLICKADD", String.valueOf(makeVoteList.size()));

        if (makeVoteList.size() < 10) { // 최대 10개까지 추가 가능
            MakeVote makeVote = new MakeVote();
            makeVoteList.add(makeVote);
            addContentCount = makeVoteList.size();
            addContent.setText("항목 추가하기(" + addContentCount + "/10)");
            makeVoteAdapter.notifyDataSetChanged();
        }

        Log.d("CONTENTCLICKADD", String.valueOf(makeVoteList.size()));

        if (makeVoteList.size() >= 2) {
            btn_erase_content.setClickable(true);
            btn_erase_content.setTextColor(Color.parseColor("#F43A6F"));
        }
    };


    /** 선택한 항목 지우기 버튼 클릭 **/
    View.OnClickListener deleteContentClickListener = v -> {
        // 선택 항목 삭제
        deleteMakeVote = makeVoteAdapter.getSelectedItems();
        makeVoteList.removeAll(deleteMakeVote);
        makeVoteAdapter.notifyDataSetChanged();

        // 다시 항목 추가하기로 이동
        boolean isVisible = makeVoteAdapter.isButtonVisible();
        makeVoteAdapter.setButtonVisible(!isVisible);
        deleteContent.setVisibility(View.GONE);
        addContent.setVisibility(View.VISIBLE);
        addContentCount = makeVoteList.size();
        addContent.setText("항목 추가하기("+addContentCount+"/10)");
        btn_erase_content.setText("항목 지우기");

        if (makeVoteList.size() < 2) {
            btn_erase_content.setClickable(false);
            btn_erase_content.setTextColor(Color.parseColor("#333232"));
            // 업로드 버튼 비활성화
            upload.setClickable(false);
            upload.setTextColor(ContextCompat.getColorStateList(BoardMakeVote.this, R.color.secondary_grey_black_10));
            upload.setBackgroundColor(Color.parseColor("#202020"));
        }
        else {
            btn_erase_content.setClickable(true);
            btn_erase_content.setTextColor(Color.parseColor("#F43A6F"));
        }
    };

    /** 항목 지우기 버튼 클릭 **/
    View.OnClickListener contentEraseClickListener = v -> {
        boolean isVisible = makeVoteAdapter.isButtonVisible();
        makeVoteAdapter.setButtonVisible(!isVisible);

        // 선택한 항목 지우기로 이동
        if(!isVisible) {
            btn_erase_content.setText("돌아가기");
            btn_erase_content.setTextColor(Color.parseColor("#F6F6F6"));
            addContent.setVisibility(View.GONE);
            deleteContent.setVisibility(View.VISIBLE);
            deleteContent.setClickable(true);
            deleteContent.setTextColor(Color.parseColor("#F43A6F"));
        }
        // 항목 추가하기로 이동
        else {
            btn_erase_content.setTextColor(Color.parseColor("#F43A6F"));
            btn_erase_content.setText("항목 지우기");
            addContent.setVisibility(View.VISIBLE);
            deleteContent.setVisibility(View.GONE);
            addContentCount = makeVoteList.size();
            addContent.setText("항목 추가하기("+addContentCount+"/10)");
            //addContent.setTextColor(Color.parseColor("#F1F1F1"));
        }
    };

    /** 익명 투표 **/
    View.OnClickListener anonyClickListener = v -> {
        if(anony.isChecked()) {
            Log.d(TAG, "체크 여부가 사실인가 1? : " + String.valueOf(anony.isChecked()));
            anony.setBackgroundResource(R.drawable.board_checkbox_yes);
            anony.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#F6F6F6")));
            tv_anony.setTextColor(Color.parseColor("#F6F6F6"));
        } else {
            Log.d(TAG, "체크 여부가 사실인가 2? : " + String.valueOf(anony.isChecked()));
            anony.setBackgroundResource(R.drawable.board_checkbox_no);
            tv_anony.setTextColor(Color.parseColor("#959698"));
            anony.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
        }
    };

    /** 복수 투표 **/
    View.OnClickListener multiClickListener = v -> {
        if(multi.isChecked()) {
            multi.setBackgroundResource(R.drawable.board_checkbox_yes);
            multi.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#F6F6F6")));
            tv_multi.setTextColor(Color.parseColor("#F6F6F6"));
        }
        else {

            multi.setBackgroundResource(R.drawable.board_checkbox_no);
            tv_multi.setTextColor(Color.parseColor("#959698"));
            multi.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
        }
    };

    /** 업로드하기 버튼 **/
    View.OnClickListener uploadClickListener = v -> {
       makeVoteAPI();
    };

    // 각 EditText들의 null값 여부 확인
    public boolean isEditTextFilled() {
        if (title.getText().toString().isEmpty() || content.getText().toString().isEmpty()) {
            return false;
        }
        if (makeVoteAdapter.getVoteList().size() <= 1)
            return false;

        for (MakeVote makeVote : makeVoteList) {
            if (makeVote.getVoteContent() == null || makeVote.getVoteContent().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    // 입력값 확인
    public void checkInputs() {
        if (isEditTextFilled() ) {
            upload.setClickable(true);
            upload.setTextColor(Color.parseColor("#202020"));
            upload.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        // upload버튼 비활성
        else {
            upload.setClickable(false);
            upload.setTextColor(ContextCompat.getColorStateList(BoardMakeVote.this, R.color.secondary_grey_black_10));
            upload.setBackgroundColor(Color.parseColor("#202020"));
        }
    }

    // EditText에 값이 존재하게 될 때
    @Override
    public void onEditTextChanged() {
        boolean isEditTextFilled = makeVoteAdapter.areEditTextsFilled();
        // upload 버튼 활성화 비활성화 여부
        if(isEditTextFilled) {
            checkInputs();
        }
        else {
            upload.setClickable(false);
            upload.setTextColor(ContextCompat.getColorStateList(BoardMakeVote.this, R.color.secondary_grey_black_10));
            upload.setBackgroundColor(Color.parseColor("#202020"));
        }
    }

    /** API 통신 **/
    public void makeVoteAPI() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);
        List<String> requestList = new ArrayList<>();

        for (MakeVote vote : makeVoteAdapter.getVoteList())
            requestList.add(vote.getVoteContent());

        String str_title = title.getText().toString();
        String str_content = content.getText().toString();

        Log.d(TAG, "teamId : " + teamId);

        BoardMakeVoteRequest br = new BoardMakeVoteRequest(requestList, str_content, str_title, multi.isChecked(), anony.isChecked());
        call = apiService.makeVote(accessToken, teamId, br);
        call.enqueue(new Callback<BoardMakeVoteResponse>() {
            @Override
            public void onResponse(Call<BoardMakeVoteResponse> call, Response<BoardMakeVoteResponse> response) {
                if(response.isSuccessful()) {
                    BoardMakeVoteResponse voteResponse = response.body();
                    String msg = voteResponse.getMessage();
                    if(msg.equals("투표를 생성하였습니다")) {
                        Long voteId = voteResponse.getData().getVoteId();
                        Intent intent = new Intent(getApplicationContext(), VoteInfoActivity.class);
                        intent.putExtra("teamId", teamId);
                        intent.putExtra("voteId", voteId);

                        startActivity(intent);
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
                            ChangeJwt.updateJwtToken(BoardMakeVote.this);
                            makeVoteAPI();
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
            public void onFailure(Call<BoardMakeVoteResponse> call, Throwable t) {
                Log.d(TAG, "투표 생성 실패...");
            }
        });
    }
}