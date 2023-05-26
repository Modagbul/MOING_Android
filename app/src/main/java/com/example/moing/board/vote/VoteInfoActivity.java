package com.example.moing.board.vote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.example.moing.R;
import com.example.moing.board.notice.NoticeVoteActivity;
import com.example.moing.request.BoardVoteDoRequest;
import com.example.moing.request.BoardVoteMakeCommentRequest;
import com.example.moing.response.BoardVoteCommentResponse;
import com.example.moing.response.BoardVoteInfoResponse;
import com.example.moing.response.BoardVoteMakeCommentResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoteInfoActivity extends AppCompatActivity {
    private static final String TAG = "VoteInfoActivity";

    Button back, voteComplete;
    ImageButton modal, send;
    TextView title, nickName, time, content, voteCount, tvAnony, tv_noread;
    CircleImageView profile;
    ImageView ivAnony, noReadArrow;
    RecyclerView voteRecycle, noReadRecycle, commentRecycle;
    CardView noreadCardView;
    LinearLayout layout_cardView;
    EditText et_comment;

    // 투표 리스트
    private List<BoardVoteInfoResponse.VoteChoice> voteChoiceList;
    // 투표 선택한 리스트
    private List<BoardVoteInfoResponse.VoteChoice> voteSelected;
    // 투표한 사람들의 이름 리스트
    private List<String> voteUserNameList;
    // 투표 안 읽은 사람의 리스트
    private List<String> voteNoReadList;
    // 댓글 리스트
    private List<BoardVoteCommentResponse.VoteData> voteCommentList;

    private VoteInfoAdapterFirst voteInfoAdapterFirst;
    private VoteNoReadAdapter voteNoReadAdapter;
    private VoteCommentAdapter voteCommentAdapter;

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private static final String USER_ID = "user_id";
    private SharedPreferences sharedPreferences;
    private Long teamId, voteId, userId, voteCommentId;
    private int activityTask;

    private Call<BoardVoteInfoResponse> boardVoteInfoResponseCall;
    private Call<BoardVoteCommentResponse> boardVoteCommentResponseCall;
    private Call<BoardVoteMakeCommentResponse> boardVoteMakeCommentResponseCall;
    private  Call<BoardVoteInfoResponse> getBoardVoteInfoResponseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_info);

        // Token을 사용할 SharedPreference
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Intent로 값 전달 받기.
        teamId = getIntent().getLongExtra("teamId", 0);
        voteId = getIntent().getLongExtra("voteId", 0);
        // 액티비티 태스크 판별을 위한 변수 설정
        activityTask = getIntent().getIntExtra("acitivityTask", -1);
        Log.d(TAG, "teamId 값 : " + teamId);
        Log.d(TAG, "voteId 값 : " + voteId);

        voteChoiceList = new ArrayList<>();
        voteSelected = new ArrayList<>();
        voteUserNameList = new ArrayList<>();
        voteNoReadList = new ArrayList<>();
        voteCommentList = new ArrayList<>();

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


        /** 투표 목록 및 선택 리사이클러뷰 **/
        // 투표하기위한 리사이클러뷰
        voteRecycle = findViewById(R.id.recycle_vote);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setSmoothScrollbarEnabled(true);
        llm.setAutoMeasureEnabled(true);

        // 투표 현황 리사이클러뷰 Layout 호출
        voteRecycle.setLayoutManager(llm);
        voteRecycle.setHasFixedSize(true);

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
        VoteNoReadGridSpacing voteNoReadGridSpacing = new VoteNoReadGridSpacing(4, grid_spacing);
        noReadRecycle.addItemDecoration(voteNoReadGridSpacing);

        // 안읽은 사람 리사이클러뷰 Layout 호출
        noReadRecycle.setLayoutManager(llm2);
        noReadRecycle.setHasFixedSize(true);

        /** 투표 안 읽은 사람 리사이클러뷰 설정 끝 **/

        // 투표 참여 인원 수
        voteCount = (TextView) findViewById(R.id.tv_count);
        // 익명 ImageView
        ivAnony = (ImageView) findViewById(R.id.iv_anony);
        // 익명 TextView
        tvAnony = (TextView) findViewById(R.id.tv_anony);

        // 투표 완료 버튼
        voteComplete = (Button) findViewById(R.id.btn_complete);
        voteComplete.setOnClickListener(completeClickListener);
        voteComplete.setClickable(false);

        // 안읽은 사람 리스트 상태 변경
        tv_noread = (TextView) findViewById(R.id.tv_noread);
        noReadArrow = (ImageView) findViewById(R.id.iv_noread);

        // 투표 댓글 리사이클러뷰
        commentRecycle = findViewById(R.id.recycle_comment);
        LinearLayoutManager llm3 = new LinearLayoutManager(this);
        llm3.setSmoothScrollbarEnabled(true);
        llm3.setAutoMeasureEnabled(true);
        // 댓글 리사이클러뷰 layout 호출
        commentRecycle.setLayoutManager(llm3);
        commentRecycle.setHasFixedSize(true);

        et_comment = (EditText) findViewById(R.id.et_comment);
        setTextWatcher(et_comment);

        send = (ImageButton) findViewById(R.id.imgbtn_send);
        send.setOnClickListener(sendClickListener);
        send.setEnabled(false);

        /** 투표 결과 API 호출 **/
        getVoteResult();
        /** 댓글 API 호출 **/
        getComment();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if(boardVoteInfoResponseCall != null)
            boardVoteInfoResponseCall.cancel();

        if(boardVoteCommentResponseCall != null)
            boardVoteCommentResponseCall.cancel();

        if(boardVoteMakeCommentResponseCall != null)
            boardVoteMakeCommentResponseCall.cancel();

        if(getBoardVoteInfoResponseCall != null)
            getBoardVoteInfoResponseCall.cancel();
    }

    /**
     * 뒤로 가기 버튼 클릭 리스너
     **/
    View.OnClickListener backClickListener = v -> {
        Intent intent = new Intent(getApplicationContext(), NoticeVoteActivity.class);
        intent.putExtra("teamId", teamId);
        intent.putExtra("NoticeOrVote", 2);
        // 목표보드에서 투표 상세로 바로 이동했을 때
        if (activityTask != 1) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        // 투표 생성 후 투표 상세로 이동했거나, 투표 목록에서 투표 상세로 이동했을 때
        startActivity(intent);
    };

    /**
     * 모달 버튼 클릭 리스너
     **/
    View.OnClickListener modalClickListener = v -> {
        VoteDeleteFragment voteDeleteFragment = new VoteDeleteFragment();
        Log.d(TAG, "voteDelete에 전달하기 위한 teamId 값 : " + teamId);
        Bundle bundle = new Bundle();
        bundle.putLong("teamId", teamId);
        bundle.putLong("voteId", voteId);
        bundle.putInt("activityTask", activityTask);
        voteDeleteFragment.setArguments(bundle);
        voteDeleteFragment.show(getSupportFragmentManager(), voteDeleteFragment.getTag());
    };

    /**
     * 투표하기 완료 버튼 클릭 리스너
     **/
    View.OnClickListener completeClickListener = v -> {
        selectComment();
    };

    /**
     * CardView(안읽은 사람 리스트) 클릭 리스너
     **/
    View.OnClickListener cardViewClickListener = v -> {
        int visibility = (noReadRecycle.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;

        TransitionManager.beginDelayedTransition(layout_cardView, new AutoTransition());
        noReadRecycle.setVisibility(visibility);
        // 보이는 상태라면
        if (visibility == View.VISIBLE)
            noReadArrow.setImageResource(R.drawable.arrow_up);
        else
            noReadArrow.setImageResource(R.drawable.arrow_down);
    };

    /**
     * 댓글 남기기 버튼 클릭 리스너
     **/
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


    /**
     * API 통신 (투표 결과 상세 조회)
     **/
    private void getVoteResult() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        apiService = RetrofitClientJwt.getApiService(accessToken);


        boardVoteInfoResponseCall = apiService.voteDetailInfo(accessToken, teamId, voteId);
        boardVoteInfoResponseCall.enqueue(new Callback<BoardVoteInfoResponse>() {

            @Override
            public void onResponse(Call<BoardVoteInfoResponse> call, Response<BoardVoteInfoResponse> response) {
                // 응답이 성공적일 때
                if (response.isSuccessful()) {
                    BoardVoteInfoResponse infoResponse = response.body();
                    // 제대로 된 연동 성공!
                    if (infoResponse.getMessage().equals("투표를 상세 조회하였습니다")) {
                        // 제목 set
                        title.setText(infoResponse.getData().getTitle());
                        // 내용 set
                        content.setText(infoResponse.getData().getMemo());

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
                        S3Utils.downloadImageFromS3(infoResponse.getData().getUserImageUrl(), new DownloadImageCallback() {
                            @Override
                            public void onImageDownloaded(byte[] data) {
                                runOnUiThread(() -> Glide.with(VoteInfoActivity.this)
                                        .load(data)
                                        .into(profile));
                            }

                            @Override
                            public void onImageDownloadFailed() {
                                runOnUiThread(() -> Glide.with(VoteInfoActivity.this)
                                        .load(infoResponse.getData().getUserImageUrl())
                                        .into(profile));
                            }
                        });


                        /** 투표, 각 투표마다 읽은 사람 리스트 설정 **/
                        voteChoiceList = infoResponse.getData().getVoteChoices();
                        boolean anonymous = infoResponse.getData().isAnonymous();
                        boolean multiple = infoResponse.getData().isMultiple();

                        /** 몇 명 참여했는지 계산 **/
                        List<String> userList = new ArrayList<>();
                        for (BoardVoteInfoResponse.VoteChoice choice : voteChoiceList) {
                            for (String nickName : choice.getVoteUserNickName()) {
                                userList.add(nickName);
                            }
                        }

                        Set<String> set = new HashSet<>(userList);
                        voteUserNameList = new ArrayList<>(set);
                        voteCount.setText(String.valueOf(voteUserNameList.size()) + "명 참여");

                        /** 익명 여부에 따른 텍스트 처리 **/
                        tvAnony.setVisibility(anonymous ? View.VISIBLE : View.INVISIBLE);

                        VoteInfoAdapterFirst voteInfoAdapterFirst = new VoteInfoAdapterFirst(voteChoiceList, voteSelected, VoteInfoActivity.this, false);
                        voteInfoAdapterFirst.setMultiAnony(multiple, anonymous);
                        voteRecycle.setAdapter(voteInfoAdapterFirst);

                        /** 투표 선택 클릭 리스너 **/
                        voteInfoAdapterFirst.setOnItemClickListener(new VoteInfoAdapterFirst.OnItemClickListener() {
                            @Override
                            public void onItemClick(int pos) {
                                voteSelected = voteInfoAdapterFirst.getSelectedItems();
                                Log.d("VoteInfoActivity", String.valueOf(voteSelected.size()));
                                if (voteSelected.size() >= 1) {
                                    voteComplete.setClickable(true);
                                    voteComplete.setTextColor(Color.parseColor("#FFFFFF"));
                                    voteComplete.setBackgroundColor(Color.parseColor("#FF725F"));
                                } else {
                                    voteComplete.setClickable(false);
                                    voteComplete.setTextColor(Color.parseColor("#37383C"));
                                    voteComplete.setBackgroundColor(Color.parseColor("#1A1919"));
                                }
                            }
                        });

                        // 투표 안 읽은 사람 리스트
                        voteNoReadList = infoResponse.getData().getNotReadUsersNickName();
                        voteNoReadAdapter = new VoteNoReadAdapter(voteNoReadList, VoteInfoActivity.this);
                        noReadRecycle.setAdapter(voteNoReadAdapter);
                        tv_noread.setText(voteNoReadList.size() + "명이 아직 안 읽었어요");

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
                                ChangeJwt.updateJwtToken(VoteInfoActivity.this);
                                getVoteResult();
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

                    Log.d(TAG, "응답 성공X, msg : " + response.message());

            }

            @Override
            public void onFailure(Call<BoardVoteInfoResponse> call, Throwable t) {
                Log.d(TAG, "투표 결과 상세 조회 연동 실패...");
            }
        });
    }


    /**
     * 투표 댓글 목록 조회
     **/
    private void getComment() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        apiService = RetrofitClientJwt.getApiService(accessToken);
        boardVoteCommentResponseCall = apiService.voteCommentInfo(accessToken, teamId, voteId);
        boardVoteCommentResponseCall.enqueue(new Callback<BoardVoteCommentResponse>() {
            @Override
            public void onResponse(Call<BoardVoteCommentResponse> call, Response<BoardVoteCommentResponse> response) {

                if (response.isSuccessful()) {
                    BoardVoteCommentResponse commentResponse = response.body();
                    if (commentResponse.getMessage().equals("투표 댓글 목록을 최신순으로 조회하였습니다")) {
                        userId = sharedPreferences.getLong(USER_ID, 0);
                        voteCommentList = commentResponse.getData();
                        VoteCommentAdapter commentAdapter = new VoteCommentAdapter(voteCommentList, VoteInfoActivity.this);
                        Log.d(TAG, "userId : " + userId);
                        commentAdapter.setUserId(userId);
                        // 삭제 버튼 클릭 리스너
                        commentAdapter.setOnCommentButtonClickListener(new VoteCommentAdapter.OnCommentButtonClickListener() {
                            @Override
                            public void onCommentButtonClick(int position) {
                                // 해당 Position의 버튼이 클릭됐을 때 수행할 동작을 구한다.
                                /** 삭제 API **/
                                Long commentId = voteCommentList.get(position).getVoteCommentId();
                                deleteVoteComment(commentId);
                            }
                        });

                        commentRecycle.setAdapter(commentAdapter);
                        commentAdapter.notifyDataSetChanged();
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
                                ChangeJwt.updateJwtToken(VoteInfoActivity.this);
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
            public void onFailure(Call<BoardVoteCommentResponse> call, Throwable t) {
                Log.d(TAG, "투표 댓글 목록 연동 실패...");
            }
        });
    }


    /**
     * 투표 댓글 생성
     **/
    private void makeComment() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        apiService = RetrofitClientJwt.getApiService(accessToken);
        String content = et_comment.getText().toString();
        BoardVoteMakeCommentRequest commentRequest = new BoardVoteMakeCommentRequest(content);
        boardVoteMakeCommentResponseCall = apiService.voteMakeComment(accessToken, teamId, voteId, commentRequest);
        boardVoteMakeCommentResponseCall.enqueue(new Callback<BoardVoteMakeCommentResponse>() {
            @Override
            public void onResponse(Call<BoardVoteMakeCommentResponse> call, Response<BoardVoteMakeCommentResponse> response) {

                if (response.isSuccessful()) {
                    BoardVoteMakeCommentResponse makeCommentResponse = response.body();
                    if (makeCommentResponse.getMessage().equals("투표의 댓글을 생성하였습니다")) {

                        voteCommentId = makeCommentResponse.getData().getVoteCommentId();

                        Log.d(TAG, "투표 댓글 연동 성공!");
                        Toast.makeText(getApplicationContext(), "댓글 생성이 완료되었습니다.", Toast.LENGTH_SHORT).show();

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
                                ChangeJwt.updateJwtToken(VoteInfoActivity.this);
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
            public void onFailure(Call<BoardVoteMakeCommentResponse> call, Throwable t) {
                Log.d(TAG, "투표 댓글 생성 연동실패... : " + t.getMessage());
            }
        });
    }


    /**
     * 투표하기 API
     **/
    private void selectComment() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        List<String> voteList = new ArrayList<>();
        for (BoardVoteInfoResponse.VoteChoice choice : voteSelected) {
            voteList.add(choice.getContent());
            Log.d("BoardVoteInfoResponse", choice.getContent() + "에 투표하셨습니다.");
        }

        BoardVoteDoRequest request = new BoardVoteDoRequest(voteList);
        getBoardVoteInfoResponseCall = apiService.voteResult(accessToken, teamId, voteId, request);
        getBoardVoteInfoResponseCall.enqueue(new Callback<BoardVoteInfoResponse>() {
            @Override
            public void onResponse(Call<BoardVoteInfoResponse> call, Response<BoardVoteInfoResponse> response) {
                BoardVoteInfoResponse infoResponse = response.body();
                if (response.isSuccessful()) {
                    if (infoResponse.getMessage().equals("투표를 하였습니다")) {
                        /** 투표, 각 투표마다 읽은 사람 리스트 설정 **/
                        voteChoiceList = infoResponse.getData().getVoteChoices();
                        voteNoReadList = infoResponse.getData().getNotReadUsersNickName();
                        boolean anonymous = infoResponse.getData().isAnonymous();
                        boolean multiple = infoResponse.getData().isMultiple();

                        VoteInfoAdapterFirst adapterFirst = new VoteInfoAdapterFirst(voteChoiceList, voteSelected, VoteInfoActivity.this, true);
                        adapterFirst.setMultiAnony(multiple, anonymous);
                        voteRecycle.setAdapter(adapterFirst);
                        adapterFirst.notifyDataSetChanged();

                        voteComplete.setText("투표 완료");
                        voteComplete.setTextColor(Color.parseColor("#37383C"));
                        voteComplete.setBackgroundColor(Color.parseColor("#1A1919"));
                        tv_noread.setText(voteNoReadList.size() + "명이 아직 안 읽었어요");

                        /** 몇 명 참여했는지 계산 **/
                        List<String> userList = new ArrayList<>();
                        for (BoardVoteInfoResponse.VoteChoice choice : voteChoiceList) {
                            for (String nickName : choice.getVoteUserNickName()) {
                                userList.add(nickName);
                            }
                        }

                        Set<String> set = new HashSet<>(userList);
                        voteUserNameList = new ArrayList<>(set);
                        voteCount.setText(String.valueOf(voteUserNameList.size()) + "명 참여");
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
                            ChangeJwt.updateJwtToken(VoteInfoActivity.this);
                            selectComment();
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
            public void onFailure(Call<BoardVoteInfoResponse> call, Throwable t) {
                Log.d(TAG, "2번 : 오류 메세지 : " + t.getMessage());
            }
        });

    }

    /**
     * 댓글 삭제 API
     **/
    private void deleteVoteComment(Long commentId) {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);
        userId = sharedPreferences.getLong(USER_ID, 0);

        Call<NoticeVoteFinishResponse> call = apiService.deleteVoteComment(accessToken, teamId, voteId, commentId);
        call.enqueue(new Callback<NoticeVoteFinishResponse>() {
            @Override
            public void onResponse(Call<NoticeVoteFinishResponse> call, Response<NoticeVoteFinishResponse> response) {
                if (response.isSuccessful()) {
                    NoticeVoteFinishResponse finishResponse = response.body();
                    if (finishResponse.getMessage().equals("투표의 댓글을 삭제하였습니다")) {
                        restartActivity();
                        Toast.makeText(getApplicationContext(), "해당 댓글이 삭제되었습니다", Toast.LENGTH_SHORT).show();
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
                            ChangeJwt.updateJwtToken(VoteInfoActivity.this);
                            deleteVoteComment(commentId);
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
                voteId = getIntent().getLongExtra("voteId", 0);
                finish();
                overridePendingTransition(0, 0); // 인텐트 애니메이션 없애기
                startActivity(intent); // 현재 액티비티 재실행
                overridePendingTransition(0, 0); // 인텐트 애니메이션 없애기
            }
        }, 500); // 1초(1000밀리초) 딜레이
    }
}