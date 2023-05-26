package com.example.moing.board.notice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.moing.R;
import com.example.moing.board.BoardActivity;
import com.example.moing.board.vote.BoardMakeVote;
import com.example.moing.board.vote.VoteInfoActivity;
import com.example.moing.board.vote.VoteViewAdapter;
import com.example.moing.response.AllNoticeResponse;
import com.example.moing.response.AllVoteResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeVoteActivity extends AppCompatActivity {
    private static final String TAG = "NoticeVoteActivity";

    private boolean fabMain_status = false;
    private FloatingActionButton fabMain;
    private ImageView fabVoteCreate;
    private ImageView fabNoticeWrite;
    private ImageButton back;
    private Long teamId, noticeId, voteId;
    private TextView tv_first, tv_second, tv_nothing;

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private SharedPreferences sharedPreferences;
    private List<AllNoticeResponse.NoticeBlock> noticeList;
    private List<AllVoteResponse.VoteBlock> voteList;

    private TabHost tabHost1;
    private TextView tv, tp;
    private int NoticeOrVote;

    // RecyclerView
    RecyclerView mRecyclerView, mRecyclerView2;

    private  Call<AllNoticeResponse> allNoticeResponseCall;
    private   Call<AllVoteResponse> allVoteResponseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_vote);

        // Intent 값 전달받는다.
        Intent intent = getIntent();
        teamId = intent.getLongExtra("teamId", 0);
        noticeId = intent.getLongExtra("noticeId", 0);
        voteId = intent.getLongExtra("voteId", 0);
        NoticeOrVote = intent.getIntExtra("NoticeOrVote", 0);

        Log.d(TAG, "NoticeOrVote : " + String.valueOf(NoticeOrVote));

        // Token을 사용할 SharedPreference
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        tv_first = findViewById(R.id.tv_toggle_text);
        tv_second = findViewById(R.id.tv3);
        tv_nothing = findViewById(R.id.tv_nothing);
        back = findViewById(R.id.btn_back);
        back.setOnClickListener(backClickListener);
        fabMain = findViewById(R.id.fabMain);
        fabVoteCreate = findViewById(R.id.vote_create);
        fabNoticeWrite = findViewById(R.id.notice_write);

        // 메인플로팅 버튼 클릭
        fabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFab();
            }
        });

        // 투표 생성하기 버튼 클릭
        fabVoteCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BoardMakeVote.class);
                intent.putExtra("teamId", teamId);
                startActivity(intent);
            }
        });

        // 공지 작성하기 버튼 클릭
        fabNoticeWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoticeWriteActivity.class);
                intent.putExtra("teamId", teamId);
                intent.putExtra("noticeId", noticeId);
                startActivity(intent);
            }
        });

        noticeList = new ArrayList<>();
        voteList = new ArrayList<>();

        // 공지사항
        mRecyclerView = findViewById(R.id.recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        // 투표
        mRecyclerView2 = findViewById(R.id.recycler2);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        linearLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView2.setLayoutManager(linearLayoutManager2);

        if(NoticeOrVote == 1) {
            /** 공지사항 **/
            notice();
        } else {
            vote();
        }

        tabHost1 = (TabHost) findViewById(R.id.tabHost1);
        tabHost1.setup();

        // 선택된 탭은 흰색, 선택되지 않은 탭은 회색
        tabHost1.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {

                // 선택되지 않은 것
                // 탭의 각 제목의 색깔을 바꾸기 위한 부분
                for (int i = 0; i < tabHost1.getTabWidget().getChildCount(); i++) {

                    tv = (TextView) tabHost1.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                    tv.setTextColor(Color.parseColor("#535457"));
                }

                // 선택된 것
                // 선택되는 탭에 대한 제목의 색깔을 바꾸 부분
                tp = (TextView) tabHost1.getTabWidget().getChildAt(tabHost1.getCurrentTab()).findViewById(android.R.id.title);
                tp.setTextColor(Color.parseColor("#FFFFFF"));

                // 선택된 탭에 대한 처리
                switch (tabHost1.getCurrentTab()) {
                    // 공지사항 탭 선택 시
                    case 0:
                        notice();
                        break;
                    case 1:
                        vote();
                        break;
                }
            }
        });

        // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
        TabHost.TabSpec ts1 = tabHost1.newTabSpec("Tab Spec 1");
        ts1.setContent(R.id.content1);
        ts1.setIndicator("공지사항");
        tabHost1.addTab(ts1);

        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
        TabHost.TabSpec ts2 = tabHost1.newTabSpec("Tab Spec 2");
        ts2.setContent(R.id.content2);
        ts2.setIndicator("투표");
        tabHost1.addTab(ts2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (allVoteResponseCall != null)
            allVoteResponseCall.cancel();

        if (allNoticeResponseCall != null)
            allNoticeResponseCall.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        for (int i = 0; i < tabHost1.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) tabHost1.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#535457"));
        }

        tp = (TextView) tabHost1.getTabWidget().getChildAt(tabHost1.getCurrentTab()).findViewById(android.R.id.title);
        tp.setTextColor(Color.parseColor("#FFFFFF"));

        if(NoticeOrVote == 1) {
            tabHost1.setCurrentTab(0);
            notice();
        }

        else {
            tabHost1.setCurrentTab(1);
            vote();
        }

    }

    // 뒤로 가기 버튼 클릭 리스너
    View.OnClickListener backClickListener = v -> {
        Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
        intent.putExtra("teamId", teamId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    };

    // 플로팅 액션 버튼 클릭시 애니메이션 효과
    public void toggleFab() {
        if (fabMain_status) {
            // 플로팅 액션 버튼 닫기
            // 애니메이션 추가
            fabVoteCreate.setVisibility(View.INVISIBLE);
            fabNoticeWrite.setVisibility(View.INVISIBLE);
            ObjectAnimator fc_animation = ObjectAnimator.ofFloat(fabVoteCreate, "translationY", 0f);
            fc_animation.start();
            ObjectAnimator fe_animation = ObjectAnimator.ofFloat(fabNoticeWrite, "translationY", 0f);
            fe_animation.start();
            // 메인 플로팅 이미지 변경
            fabMain.setImageResource(R.drawable.floating);

        } else {
            // 플로팅 액션 버튼 열기
            fabVoteCreate.setVisibility(View.VISIBLE);
            fabNoticeWrite.setVisibility(View.VISIBLE);

            ObjectAnimator fc_animation = ObjectAnimator.ofFloat(fabVoteCreate, "translationY", dpToPx(-80));
            fc_animation.start();
            ObjectAnimator fe_animation = ObjectAnimator.ofFloat(fabNoticeWrite, "translationY", dpToPx(-140));
            fe_animation.start();

            ObjectAnimator fc_animation2 = ObjectAnimator.ofFloat(fabVoteCreate, "translationX", dpToPx(-40));
            fc_animation2.start();
            ObjectAnimator fe_animation2 = ObjectAnimator.ofFloat(fabNoticeWrite, "translationX", dpToPx(-40));
            fe_animation2.start();

            // 메인 플로팅 이미지 변경
            fabMain.setImageResource(R.drawable.exit_floating);
        }
        // 플로팅 버튼 상태 변경
        fabMain_status = !fabMain_status;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }



    /**
     * 공지사항 모든 목록 출력 API
     **/
    public void notice() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        allNoticeResponseCall = apiService.viewNotice(accessToken, teamId);
        allNoticeResponseCall.enqueue(new Callback<AllNoticeResponse>() {
            @Override
            public void onResponse(Call<AllNoticeResponse> call, Response<AllNoticeResponse> response) {
                AllNoticeResponse noticeResponse = response.body();
                String msg = noticeResponse.getMessage();
                if (msg.equals("공지를 전체 조회하였습니다")) {
                    noticeList = noticeResponse.getData().getNoticeBlocks();
                    NoticeViewAdapter adapter = new NoticeViewAdapter(noticeList, NoticeVoteActivity.this);
                    mRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    if (noticeList.size() == 0)
                        tv_nothing.setVisibility(View.VISIBLE);
                    else
                        tv_nothing.setVisibility(View.GONE);

                    List<Long> noticeIdList = new ArrayList<>();
                    for (AllNoticeResponse.NoticeBlock v : noticeList) {
                        noticeIdList.add(v.getNoticeId());
                    }

                    Long num = noticeResponse.getData().getNotReadNum();
                    checkNoRead(num, "공지");

                    /** 리사이클러뷰 아이템 클릭 이벤트 처리 **/
                    adapter.setOnItemClickListener(new NoticeViewAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int pos) {
                            /** 해당 공지사항으로 이동 **/
                            noticeId = noticeIdList.get(pos);
                            Intent intent = new Intent(NoticeVoteActivity.this, NoticeInfoActivity.class);
                            intent.putExtra("noticeId", noticeId);
                            intent.putExtra("teamId", teamId);
                            startActivity(intent);
                        }
                    });
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
                            ChangeJwt.updateJwtToken(NoticeVoteActivity.this);
                            notice();
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
            public void onFailure(Call<AllNoticeResponse> call, Throwable t) {
                Log.d(TAG, "공지 전체 조회 실패...");
            }
        });
    }

    /**
     * 투표 모두 조회 API
     **/
    public void vote() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        allVoteResponseCall = apiService.viewVote(accessToken, teamId);
        allVoteResponseCall.enqueue(new Callback<AllVoteResponse>() {
            @Override
            public void onResponse(Call<AllVoteResponse> call, Response<AllVoteResponse> response) {
                AllVoteResponse voteResponse = response.body();
                String msg = voteResponse.getMessage();
                if (msg.equals("투표를 전체 조회하였습니다")) {
                    voteList = voteResponse.getData().getVoteBlocks();
                    VoteViewAdapter adapter = new VoteViewAdapter(voteList, NoticeVoteActivity.this);
                    mRecyclerView2.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    if(voteList.size() == 0)
                        tv_nothing.setVisibility(View.VISIBLE);
                    else
                        tv_nothing.setVisibility(View.GONE);


                    List<Long> voteIdList = new ArrayList<>();
                    for (AllVoteResponse.VoteBlock v : voteList) {
                        voteIdList.add(v.getVoteId());
                    }

                    Long num = voteResponse.getData().getNotReadNum();
                    checkNoRead(num, "투표");

                    /** **/
                    adapter.voteClickListener(new NoticeViewAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int pos) {
                            /** 해당 투표로 이동 **/
                            voteId = voteIdList.get(pos);
                            Intent intent = new Intent(NoticeVoteActivity.this, VoteInfoActivity.class);
                            intent.putExtra("voteId", voteId);
                            intent.putExtra("teamId", teamId);
                            startActivity(intent);
                        }
                    });
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
                            ChangeJwt.updateJwtToken(NoticeVoteActivity.this);
                            vote();
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
            public void onFailure(Call<AllVoteResponse> call, Throwable t) {
                Log.d(TAG, "투표 전체 조회 실패...");
            }
        });
    }

    public void checkNoRead(Long num, String str) {
        String text = str.equals("공지") ? "공지" : "투표";
        Log.d(TAG, "checkNoRead의 text : " + text);
        String result = "확인하지 않은 " + text + "가 " + num + "개 있어요";
        // 확인하지 않은 공지가 "3개" 있어요 할때 "3개"만 글자 색상 변경
        SpannableString sp = new SpannableString(result); // 객체 생성
        String word = num + "개";
        int start = result.indexOf(word);
        int end = start + word.length();
        sp.setSpan(new ForegroundColorSpan(Color.parseColor("#FF725F")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_first.setText(sp);

        ColorStateList co = ColorStateList.valueOf(getResources().getColor(R.color.secondary_grey_black_1));
        ColorStateList co2 = ColorStateList.valueOf(getResources().getColor(R.color.secondary_grey_black_13));

        // List의 개수가 0개일 때
        tv_second.setText(num == 0 ? "성실왕 소모임원!" : "기다리고 있을 소모임원들을 위해, 빠르게 확인해주세요!");
    }
}