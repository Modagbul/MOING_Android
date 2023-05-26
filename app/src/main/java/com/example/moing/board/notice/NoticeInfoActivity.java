package com.example.moing.board.notice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.moing.R;
import com.example.moing.board.vote.VoteCommentAdapter;
import com.example.moing.request.NoticeCommentRequest;
import com.example.moing.response.NoticeCommentListResponse;
import com.example.moing.response.NoticeCommentResponse;
import com.example.moing.response.NoticeInfoResponse;
import com.example.moing.response.NoticeVoteFinishResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;
import com.example.moing.s3.DownloadImageCallback;
import com.example.moing.s3.S3Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeInfoActivity extends AppCompatActivity {
    private static final String TAG = "NoticeInfoActivity";

    Button back;
    ImageButton modal, send;
    TextView title, nickName, time, content, tv_noread;
    CircleImageView profile;
    ImageView noReadArrow;
    RecyclerView noReadRecycle, commentRecycle;
    CardView noreadCardView;
    LinearLayout layout_cardView;
    EditText et_comment;

    // 공지 안 읽은 사람의 리스트
    private List<String> noticeNoReadList;
    // 댓글 리스트
    private List<NoticeCommentListResponse.NoticeComment> noticeCommentList;
    private NoticeNoReadAdapter noticeNoReadAdapter;
    private NoticeCommentAdapter noticeCommentAdapter;

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private static final String USER_ID = "user_id";
    private SharedPreferences sharedPreferences;
    private Long teamId, noticeId, userId, noticeCommentId;
    private int activityTask;

    private  Call<NoticeInfoResponse> noticeInfoResponseCall;
    private Call<NoticeCommentListResponse> noticeCommentListResponseCall;
    private  Call<NoticeCommentResponse> noticeCommentResponseCall;
    private Context context;
    private RequestManager glideManager; // RequestManager 객체 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_info);

        // Token을 사용할 SharedPreference
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Intent로 값 전달 받기.
        teamId = getIntent().getLongExtra("teamId", 0);
        noticeId = getIntent().getLongExtra("noticeId", 0);

        // 액티비티 태스크 판별을 위한 변수 설정
        activityTask = getIntent().getIntExtra("acitivityTask", -1);

        noticeNoReadList = new ArrayList<>();
        noticeCommentList = new ArrayList<>();

        // 뒤로 가기 버튼
        back = (Button) findViewById(R.id.btn_close);
        back.setOnClickListener(backClickListener);

        /** 모달 버튼 구현 예정 **/
        modal = (ImageButton) findViewById(R.id.imgbtn_dotIndicator);
        modal.setOnClickListener(modalClickListener);

        // 제목
        title = (TextView) findViewById(R.id.tv_title);
        // 프로필사진
        profile = findViewById(R.id.iv_profile);
        // 닉네임
        nickName = (TextView) findViewById(R.id.tv_name);
        // 날짜, 시간
        time = (TextView) findViewById(R.id.tv_time);
        // 투표 내용
        content = (TextView) findViewById(R.id.tv_content);

        // 안읽은 사람 리스트 상태 변경
        tv_noread = (TextView) findViewById(R.id.tv_noread);
        noReadArrow = (ImageView) findViewById(R.id.iv_noread);

        /** 투표 안 읽은 사람 리사이클러뷰 **/
        noReadRecycle = findViewById(R.id.recycle_noread);
        GridLayoutManager llm2 = new GridLayoutManager(this, 4);
        llm2.setSmoothScrollbarEnabled(true);
        llm2.setAutoMeasureEnabled(true);

        /** CardView 작성**/
        noreadCardView = (CardView) findViewById(R.id.cardView_noread);
        noreadCardView.setOnClickListener(cardViewClickListener);
        layout_cardView = findViewById(R.id.layout_card);
        layout_cardView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        // 안읽은 사람 리스트(GridLayout) 간격 설정
        int grid_spacing = (int) getResources().getDimension(R.dimen.grid_spacing); // 8dp 의미.
        NoticeNoReadGridSpacing noticeNoReadGridSpacing = new NoticeNoReadGridSpacing(4, grid_spacing);
        noReadRecycle.addItemDecoration(noticeNoReadGridSpacing);

        // 안읽은 사람 리사이클러뷰 Layout 호출
        noReadRecycle.setLayoutManager(llm2);
        noReadRecycle.setHasFixedSize(true);

        // 공지 댓글 리사이클러뷰
        commentRecycle = findViewById(R.id.recycle_comment);
        LinearLayoutManager llm3 = new LinearLayoutManager(this);
        llm3.setSmoothScrollbarEnabled(true);
        llm3.setAutoMeasureEnabled(true);

        commentRecycle.setLayoutManager(llm3);
        commentRecycle.setHasFixedSize(true);

        noticeCommentAdapter = new NoticeCommentAdapter(noticeCommentList, this);
        commentRecycle.setAdapter(noticeCommentAdapter);
        commentRecycle.setHasFixedSize(true);

        et_comment = (EditText) findViewById(R.id.et_comment);
        setTextWatcher(et_comment);

        send = (ImageButton) findViewById(R.id.imgbtn_send);
        send.setOnClickListener(sendClickListener);
        send.setEnabled(false);

        /** 투표 결과 API 호출 **/
        getNoticeResult();

        /** 댓글 API 호출 **/
        getComment();

    }

    @Override
    protected void onStart() {
        super.onStart();
        context = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(glideManager != null){
            glideManager.clear(profile);
        }

        if(noticeInfoResponseCall != null)
            noticeInfoResponseCall.cancel();

        if(noticeCommentListResponseCall != null)
            noticeCommentListResponseCall.cancel();

        if(noticeCommentResponseCall != null)
            noticeCommentResponseCall.cancel();
    }

    /** 뒤로 가기 버튼 클릭 리스너 **/
    View.OnClickListener backClickListener = v -> {
        // 목표보드에서 공지 상세로 바로 이동했을 때
        if(activityTask == 1) {
            Intent intent = new Intent(getApplicationContext(), NoticeVoteActivity.class);
            intent.putExtra("teamId", teamId);
            intent.putExtra("NoticeOrVote", 1);
            startActivity(intent);
        }
        // 공지 생성 후 공지 상세로 이동했거나, 공지 목록에서 공지 상세로 이동했을 때
        else {
            Intent intent = new Intent(getApplicationContext(), NoticeVoteActivity.class);
            intent.putExtra("teamId", teamId);
            intent.putExtra("NoticeOrVote", 1);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };

    /** 모달 버튼 클릭 리스너 **/
    View.OnClickListener modalClickListener = v -> {
        NoticeDeleteFragment noticeDeleteFragment = new NoticeDeleteFragment();
        Log.d(TAG, "voteDelete에 전달하기 위한 teamId 값 : " + teamId);
        Bundle bundle = new Bundle();
        bundle.putLong("teamId", teamId);
        bundle.putLong("noticeId", noticeId);
        bundle.putInt("activityTask", activityTask);
        noticeDeleteFragment.setArguments(bundle);
        noticeDeleteFragment.show(getSupportFragmentManager(), noticeDeleteFragment.getTag());

    };

    /** CardView(안읽은 사람 리스트) 클릭 리스너 **/
    View.OnClickListener cardViewClickListener = v -> {
        int visibility = (noReadRecycle.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;

        TransitionManager.beginDelayedTransition(layout_cardView, new AutoTransition());
        noReadRecycle.setVisibility(visibility);
        // 보이는 상태라면
        if(visibility == View.VISIBLE)
            noReadArrow.setImageResource(R.drawable.arrow_up);
        else
            noReadArrow.setImageResource(R.drawable.arrow_down);
    };

    View.OnClickListener sendClickListener = v -> {
        String s = et_comment.getText().toString();
        if (!TextUtils.isEmpty(s)) {
            /** 스레드 순서 처리 **/
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//                    makeComment();
//                    return null;
//                }).thenRunAsync(() -> {
//                    runOnUiThread(() -> {
//                        getComment();
//                    });
//                });
//                future.join();
//            }
            // 결과를 기다림
            /** 화면 새로고침 **/
            makeComment();
            getComment();
            restartActivity();
        }
    };

    /**
     * 댓글 남기기 입력창 관리
     **/
    private void setTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    send.setEnabled(true);
                } else
                    send.setClickable(false);
            }
        });
    }

    /** API 통신 (공지 상세 조회) **/
    private void getNoticeResult() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        apiService = RetrofitClientJwt.getApiService(accessToken);

        Log.d(TAG, "noticeId : " + String.valueOf(noticeId));

        noticeInfoResponseCall = apiService.NoticeInfo(accessToken, teamId, noticeId);
        noticeInfoResponseCall.enqueue(new Callback<NoticeInfoResponse>() {
            @Override
            public void onResponse(Call<NoticeInfoResponse> call, Response<NoticeInfoResponse> response) {
                // 응답이 성공적일 때
                if (response.isSuccessful()) {
                    NoticeInfoResponse infoResponse = response.body();
                    // 제대로 된 연동 성공!
                    if (infoResponse.getMessage().equals("공지를 상세 조회하였습니다")) {
                        // 제목 set
                        title.setText(infoResponse.getData().getTitle());
                        // 내용 set
                        content.setText(infoResponse.getData().getContent());
                        // userId set
                        userId = infoResponse.getData().getUserId();

                        // 시간 set
                        String tmpTime = infoResponse.getData().getCreatedDate();
                        String[] tmp = tmpTime.split("T");
                        String date = tmp[0].substring(5, 10);
                        date = date.replace('-', '/');
                        String getTime = tmp[1].substring(0, 5);
                        String realTime = date + " " + getTime;

                        time.setText(realTime);
                        // 닉네임 set
                        nickName.setText(infoResponse.getData().getNickName());
                        /**S3Glide**/
//                        Glide.with(NoticeInfoActivity.this)
//                                .load(infoResponse.getData().getUserImageUrl())
//                                .into(profile);
                        S3Utils.downloadImageFromS3(infoResponse.getData().getUserImageUrl(), new DownloadImageCallback() {

                            @Override
                            public void onImageDownloaded(byte[] data) {
                                if(context != null){
                                    runOnUiThread(() ->{
                                        glideManager = Glide.with(context);

                                        glideManager
                                                .asBitmap()
                                                .load(data)
                                                .into(profile);
                                    });
                                }
                            }
                            @Override
                            public void onImageDownloadFailed() {
                                if(context != null){
                                    runOnUiThread(() ->{
                                        glideManager = Glide.with(context);

                                        glideManager
                                                .asBitmap()
                                                .load(infoResponse.getData().getUserImageUrl())
                                                .into(profile);
                                    });
                                }
                            }
                        });


                        // 투표 안 읽은 사람 리스트
                        noticeNoReadList = infoResponse.getData().getNotReadUsersNickName();
                        noticeNoReadAdapter = new NoticeNoReadAdapter(noticeNoReadList, NoticeInfoActivity.this);
                        noReadRecycle.setAdapter(noticeNoReadAdapter);
                        tv_noread.setText(noticeNoReadList.size() + "명이 아직 안 읽었어요");

                    }else{
                        try {
                            /** 작성자가 아닌 경우 **/
                            String errorJson = response.errorBody().string();
                            JSONObject errorObject = new JSONObject(errorJson);
                            // 에러 코드로 에러처리를 하고 싶을 때
                            // String errorCode = errorObject.getString("errorCode");
                            /** 메세지로 에러처리를 구분 **/
                            String message = errorObject.getString("message");

                            if (message.equals("만료된 토큰입니다.")) {
                                ChangeJwt.updateJwtToken(NoticeInfoActivity.this);
                                getNoticeResult();
                            }

                        } catch (IOException e) {
                            // 에러 응답의 JSON 문자열을 읽을 수 없을 때
                            e.printStackTrace();
                        } catch (JSONException e) {
                            // JSON 객체에서 필드 추출에 실패했을 때
                            e.printStackTrace();
                        }
                    }

                } else
                    Log.d(TAG, "응답 성공X, msg : " + response.message().toString());
            }

            @Override
            public void onFailure(Call<NoticeInfoResponse> call, Throwable t) {
                Log.d(TAG, "공지 상세 조회 연동 실패...");
            }
        });
    }

    /** 공지 댓글 목록 조회 **/
    private void getComment() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        apiService = RetrofitClientJwt.getApiService(accessToken);
        noticeCommentListResponseCall = apiService.makeNoticeCommentList(accessToken, teamId, noticeId);
        noticeCommentListResponseCall.enqueue(new Callback<NoticeCommentListResponse>() {
            @Override
            public void onResponse(Call<NoticeCommentListResponse> call, Response<NoticeCommentListResponse> response) {
                if(response.isSuccessful()) {
                    NoticeCommentListResponse commentResponse = response.body();
                    if(commentResponse.getMessage().equals("공지 댓글 목록을 최신순으로 조회하였습니다"))
                    {
                        userId = sharedPreferences.getLong(USER_ID, 0);
                        noticeCommentList = commentResponse.getData();
                        NoticeCommentAdapter commentAdapter = new NoticeCommentAdapter(noticeCommentList, NoticeInfoActivity.this);
                        commentAdapter.setUserId(userId);
                        // 삭제버튼 클릭 리스너
                        commentAdapter.setOnCommentButtonClickListener(new VoteCommentAdapter.OnCommentButtonClickListener() {
                            @Override
                            public void onCommentButtonClick(int position) {
                                /** 삭제 API **/
                                Long commentId = noticeCommentList.get(position).getNoticeCommentId();
                                deleteNoticeComment(commentId);
                            }
                        });

                        commentRecycle.setAdapter(commentAdapter);
                        commentAdapter.notifyDataSetChanged();
                    }
                    else{
                        try {
                            /** 작성자가 아닌 경우 **/
                            String errorJson = response.errorBody().string();
                            JSONObject errorObject = new JSONObject(errorJson);
                            // 에러 코드로 에러처리를 하고 싶을 때
                            // String errorCode = errorObject.getString("errorCode");
                            /** 메세지로 에러처리를 구분 **/
                            String message = errorObject.getString("message");

                            if (message.equals("만료된 토큰입니다.")) {
                                ChangeJwt.updateJwtToken(NoticeInfoActivity.this);
                                getComment();
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
            }

            @Override
            public void onFailure(Call<NoticeCommentListResponse> call, Throwable t) {
                Log.d(TAG, "공지 댓글 목록 연동 실패...");
            }
        });
    }

    /** 공지 댓글 생성 **/
    private void makeComment() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        apiService = RetrofitClientJwt.getApiService(accessToken);
        String content = et_comment.getText().toString();
        NoticeCommentRequest commentRequest = new NoticeCommentRequest(content);
        noticeCommentResponseCall = apiService.makeNoticeComment(accessToken, teamId, noticeId, commentRequest);
        noticeCommentResponseCall.enqueue(new Callback<NoticeCommentResponse>() {
            @Override
            public void onResponse(Call<NoticeCommentResponse> call, Response<NoticeCommentResponse> response) {
                if(response.isSuccessful()) {
                    NoticeCommentResponse makeCommentResponse = response.body();
                    if(makeCommentResponse.getMessage().equals("공지의 댓글을 생성하였습니다")) {
                        noticeCommentId = makeCommentResponse.getData().getNoticeCommentId();

                        Log.d(TAG, "공지 댓글 연동 성공!");
                        Toast.makeText(getApplicationContext(), "댓글 생성이 완료되었습니다.", Toast.LENGTH_SHORT).show();
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
                                ChangeJwt.updateJwtToken(NoticeInfoActivity.this);
                                makeComment();
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
            }

            @Override
            public void onFailure(Call<NoticeCommentResponse> call, Throwable t) {
                Log.d(TAG, "공지 댓글 생성 연동실패... : " + t.getMessage());
            }
        });
    }

    private void deleteNoticeComment(Long commentId) {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);
        userId = sharedPreferences.getLong(USER_ID, 0);

        Call<NoticeVoteFinishResponse> call = apiService.deleteNoticeComment(accessToken, teamId, noticeId, commentId);
        call.enqueue(new Callback<NoticeVoteFinishResponse>() {
            @Override
            public void onResponse(Call<NoticeVoteFinishResponse> call, Response<NoticeVoteFinishResponse> response) {
                if(response.isSuccessful()) {
                    NoticeVoteFinishResponse finishResponse = response.body();
                    if(finishResponse.getMessage().equals("공지의 댓글을 삭제하였습니다")) {
                        restartActivity();
                        Toast.makeText(getApplicationContext(), "해당 댓글이 삭제되었습니다", Toast.LENGTH_SHORT).show();
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
                            ChangeJwt.updateJwtToken(NoticeInfoActivity.this);
                            deleteNoticeComment(commentId);
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
            public void onFailure(Call<NoticeVoteFinishResponse> call, Throwable t) {
                Log.d(TAG, "댓글 삭제 실패 ..." + t.getMessage());
            }
        });
    }

    private void restartActivity() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                teamId = getIntent().getLongExtra("teamId", 0);
                noticeId = getIntent().getLongExtra("noticeId", 0);
                finish();
                overridePendingTransition(0, 0); // 인텐트 애니메이션 없애기
                startActivity(intent); // 현재 액티비티 재실행
                overridePendingTransition(0, 0); // 인텐트 애니메이션 없애기
            }
        }, 500); // 1초(1000밀리초) 딜레이
    }
}
