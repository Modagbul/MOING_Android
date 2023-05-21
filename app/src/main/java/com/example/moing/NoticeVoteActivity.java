package com.example.moing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import android.widget.Toast;

import com.example.moing.Response.AllNoticeResponse;
import com.example.moing.Response.AllVoteResponse;
import com.example.moing.board.BoardActivity;
import com.example.moing.board.BoardMakeVote;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

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
    private Long teamId;
    private TextView tv_first, tv_second;

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private SharedPreferences sharedPreferences;
    private List<AllNoticeResponse.NoticeBlock> noticeList;
    private List<AllVoteResponse.VoteBlock> voteList;

    // RecyclerView
    RecyclerView mRecyclerView, mRecyclerView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_vote);

        // Intent 값 전달받는다.
        Intent intent = getIntent();
        teamId = intent.getLongExtra("teamId", 0);

        // Token을 사용할 SharedPreference
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        tv_first = findViewById(R.id.tv_toggle_text);
        tv_second = findViewById(R.id.tv3);
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

        /** 공지사항 **/
        notice();

        /** 투표 **/
        vote();


        TabHost tabHost1 = (TabHost) findViewById(R.id.tabHost1);
        tabHost1.setup();

        // 선택된 탭은 흰색, 선택되지 않은 탭은 회색
        tabHost1.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {

                // 선택되지 않은 것
                // 탭의 각 제목의 색깔을 바꾸기 위한 부분
                for (int i = 0; i < tabHost1.getTabWidget().getChildCount(); i++) {

                    TextView tv = (TextView) tabHost1.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                    tv.setTextColor(Color.parseColor("#535457"));
                }

                // 선택된 것
                // 선택되는 탭에 대한 제목의 색깔을 바꾸 부분
                TextView tp = (TextView) tabHost1.getTabWidget().getChildAt(tabHost1.getCurrentTab()).findViewById(android.R.id.title);
                tp.setTextColor(Color.parseColor("#FFFFFF"));

                // 선택된 탭에 대한 처리
                switch(tabHost1.getCurrentTab()) {
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

    // 뒤로 가기 버튼 클릭 리스너
    View.OnClickListener backClickListener = v -> {
      finish();
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

            ObjectAnimator fc_animation = ObjectAnimator.ofFloat(fabVoteCreate, "translationY", -80f);
            fc_animation.start();
            ObjectAnimator fe_animation = ObjectAnimator.ofFloat(fabNoticeWrite, "translationY", -140f);
            fe_animation.start();

            ObjectAnimator fc_animation2 = ObjectAnimator.ofFloat(fabVoteCreate, "translationX", -40f);
            fc_animation2.start();
            ObjectAnimator fe_animation2 = ObjectAnimator.ofFloat(fabNoticeWrite, "translationX", -40f);
            fe_animation2.start();

            // 메인 플로팅 이미지 변경
            fabMain.setImageResource(R.drawable.exit_floating);
        }
        // 플로팅 버튼 상태 변경
        fabMain_status = !fabMain_status;
    }

    /** 공지사항 모든 목록 출력 API **/
    public void notice() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        Call<AllNoticeResponse> call = apiService.viewNotice(accessToken, teamId);
        call.enqueue(new Callback<AllNoticeResponse>() {
            @Override
            public void onResponse(Call<AllNoticeResponse> call, Response<AllNoticeResponse> response) {
                AllNoticeResponse noticeResponse = response.body();
                String msg = noticeResponse.getMessage();
                if(msg.equals("공지를 전체 조회하였습니다")) {
                    noticeList = noticeResponse.getData().getNoticeBlocks();

                    NoticeViewAdapter adapter = new NoticeViewAdapter(noticeList, NoticeVoteActivity.this);
                    mRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    if(noticeList.size() == 0) {

                    }
                    Long num = noticeResponse.getData().getNotReadNum();
                    checkNoRead(num, "공지");

                    adapter.setOnItemClickListener(new NoticeViewAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int pos) {
                            /** 해당 공지사항으로 이동 **/
                            String s = pos + "번 메뉴 선택!";
                            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else if (msg.equals("만료된 토큰입니다.")) {
                    ChangeJwt.updateJwtToken(NoticeVoteActivity.this);
                    notice();
                }
            }

            @Override
            public void onFailure(Call<AllNoticeResponse> call, Throwable t) {
                Log.d(TAG, "공지 전체 조회 실패...");
            }
        });
    }

    /** 투표 모두 조회 API **/
    public void vote() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        Call<AllVoteResponse> call = apiService.viewVote(accessToken, teamId);
        call.enqueue(new Callback<AllVoteResponse>() {
            @Override
            public void onResponse(Call<AllVoteResponse> call, Response<AllVoteResponse> response) {
                AllVoteResponse voteResponse = response.body();
                String msg = voteResponse.getMessage();
                if(msg.equals("투표를 전체 조회하였습니다")) {
                    voteList = voteResponse.getData().getVoteBlocks();
                    VoteViewAdapter adapter = new VoteViewAdapter(voteList, NoticeVoteActivity.this);
                    mRecyclerView2.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    Long num = voteResponse.getData().getNotReadNum();
                    checkNoRead(num, "공지");
                }
                else if (msg.equals("만료된 토큰입니다.")) {
                    ChangeJwt.updateJwtToken(NoticeVoteActivity.this);
                    vote();
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

        Log.d(TAG, "수행완료1");
        // List의 개수가 0개일 때
        tv_second.setText(num == 0 ? "성실왕 소모임원!" : "기다리고 있을 소모임원들을 위해, 빠르게 확인해주세요!");
    }
}