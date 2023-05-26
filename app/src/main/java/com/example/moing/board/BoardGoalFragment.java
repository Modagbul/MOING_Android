package com.example.moing.board;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.Target;
import com.example.moing.R;

import com.example.moing.board.notice.NoticeInfoActivity;
import com.example.moing.board.notice.NoticeVoteActivity;
import com.example.moing.response.BoardCurrentLocateResponse;
import com.example.moing.response.BoardFireResponse;
import com.example.moing.response.BoardMoimResponse;
import com.example.moing.response.BoardNoReadNoticeResponse;
import com.example.moing.response.BoardNoReadVoteResponse;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardGoalFragment extends Fragment {
    private static final String TAG = "BoardGoalFragment";

    ScrollView scroll;
    TextView teamName, curDate, userName, tv_toggle, tv_all, tv_all2;
    TextView btn_notice, btn_vote, tv_hot, tv_progress_team, tv_progress_user;
    ImageView notice_sign, vote_sign;
    ImageButton dot, down;
    ImageView teamImg, btnRefresh, fire, imgTeam, imgUser;
    Button teamDay, btnAll;

    // Dot Indicator
    BoardDotFragment boardDotFragment;

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private SharedPreferences sharedPreferences;

    // RecyclerView
    RecyclerView recyclerView;
    BoardGoalAdapter<?> boardGoalAdapter;

    // API 연동시 필요한 변수
    String name, profileImg, remainPeriod, nowTime;
    Long teamId, voteId, noticeId;
    List<BoardNoReadNoticeResponse.NoticeData> noticeDataList;
    List<BoardNoReadVoteResponse.VoteData> voteDataList;
    private int noReadNotice, noReadVote, acitivityTask;
    private Long personalRate, teamRate;
    RelativeLayout relative_progress;

    private  Call<BoardMoimResponse> boardMoimResponseCall;
    private  Call<BoardFireResponse> boardFireResponseCall;
    private Call<BoardNoReadNoticeResponse> boardNoReadNoticeResponseCall;
    private Call<BoardNoReadVoteResponse> boardNoReadVoteResponseCall;
    private Call<BoardCurrentLocateResponse> boardCurrentLocateResponseCall;
    private Context context; // 컨텍스트 변수 추가
    private RequestManager glideManager; // RequestManager 객체 선언

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context; // 컨텍스트 저장
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board_goal, container, false);

        // Token을 사용할 SharedPreference
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Intent로 값 받기
        teamId = getActivity().getIntent().getLongExtra("teamId", 0);
        Log.d(TAG, "teamId 값 : " + teamId);

        // 버튼 클릭 리스너 객체 생성
        BtnOnClickListener btnOnClickListener = new BtnOnClickListener();

        // 스크롤뷰
        scroll = view.findViewById(R.id.scrollView);
        // 소모임 이름
        teamName = view.findViewById(R.id.tv_team);
        // Dot Indicator
        dot = view.findViewById(R.id.imgbtn_dotIndicator);
        // 배경 사진
        teamImg = view.findViewById(R.id.img);
        // 종료 일자
        teamDay = view.findViewById(R.id.btn_dDay);
        // 새로고침 날짜, 시간
        curDate = view.findViewById(R.id.tv_date);
        // 새로고침 버튼
        btnRefresh = view.findViewById(R.id.imgBtn_refresh);
        // 불 이미지
        fire = view.findViewById(R.id.iv_fire);
        // 불 텍스트
        tv_hot = view.findViewById(R.id.tv_hot);
        // 프로그레스바 RelativeLayout
        relative_progress = view.findViewById(R.id.relative_progress);
        // 프로그레스바 User
        tv_progress_user = view.findViewById(R.id.tv_progress_user);
        // 프로그레스바 TEAM
        tv_progress_team = view.findViewById(R.id.tv_progress_team);
        // 유저 닉네임 텍스트뷰
        userName = view.findViewById(R.id.tv_userHere);
        // 팀 불 프로그레스 이미지
        imgTeam = view.findViewById(R.id.iv_team);
        // 유저 불 프로그레스 이미지
        imgUser = view.findViewById(R.id.iv_user);
        // 다운 버튼
        down = view.findViewById(R.id.imgbtn_down);
        // 공지사항, 투표
        btn_notice = view.findViewById(R.id.btn_notice);
        btn_vote = view.findViewById(R.id.btn_vote);
        notice_sign = view.findViewById(R.id.iv_notice_sign);
        vote_sign = view.findViewById(R.id.iv_vote_sign);
        // 공지 or 투표 확인 텍스트뷰
        tv_toggle = view.findViewById(R.id.tv_toggle_text);
        // 공지 모두 읽었을 때 TextView
        tv_all = (TextView) view.findViewById(R.id.tv_all);
        tv_all2 = (TextView) view.findViewById(R.id.tv_all2);
        // 공지 전체보기 버튼
        btnAll = (Button) view.findViewById(R.id.btn_all);

        /** 원래 위치 **/
        // 안 읽은 공지, 투표의 개수 초기값 설정
        noReadNotice = noReadVote = -1;

        // 리사이클러뷰
        recyclerView = view.findViewById(R.id.recycle_toggle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        // 리싸이클러뷰 스크롤 방지
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        /** 공지 안 읽은 순을 위한 ArrayList **/
        noticeDataList = new ArrayList<>();
        /** 투표 안 읽은 순을 위한 ArrayList **/
        voteDataList = new ArrayList<>();

        /** API 통신 **/
        // 소모임 제목, 사진, d-day, 날짜
        getApi();
        /** 불 그래픽 통신 **/
        apiFire();
        /** 안 읽은 투표 API **/
        //noReadVote(); // voteDataList, noReadVote 값 변경됨!!
        /** 안 읽은 공지 API **/
        noReadNotice(); // noticeDataList, noReadNotice 값 변경됨!!
        /** 팀, 나의 위치 API **/
        curLocation();

        /** API 통신 완료 **/


        /** 버튼 클릭 리스너 **/
        // Dot Indicator, 새로고침, down 버튼 클릭 리스너
        dot.setOnClickListener(btnOnClickListener);
        btnRefresh.setOnClickListener(btnOnClickListener);
        down.setOnClickListener(btnOnClickListener);
        // 공지버튼, 투표버튼 클릭 리스너
        btn_notice.setOnClickListener(btnOnClickListener);
        btn_vote.setOnClickListener(btnOnClickListener);
        // 공지 전체 보기 클릭 리스너
        btnAll.setOnClickListener(btnOnClickListener);

        // 스크롤 변화시 이미지 삭제
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY > oldScrollY) {
                        // 스크롤 다운 시 다운 버튼 숨김
                        down.setVisibility(View.GONE);
                    }
                }
            });
        }
        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (glideManager != null) {
            glideManager.clear(teamImg); // Glide 이미지 로딩 취소
        }

        if(boardMoimResponseCall != null)
            boardMoimResponseCall.cancel();

        if(boardFireResponseCall != null)
            boardFireResponseCall.cancel();

        if(boardNoReadNoticeResponseCall != null)
            boardNoReadNoticeResponseCall.cancel();

        if(boardNoReadVoteResponseCall != null)
            boardNoReadVoteResponseCall.cancel();

        if(boardCurrentLocateResponseCall != null)
            boardCurrentLocateResponseCall.cancel();

    }

    @Override
    public void onResume() {
        super.onResume();
        // 소모임 정보 수정 후 정보 업데이트
        getApi();

    }


    private class BtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // dot Indicator 버튼 클릭 시
                case R.id.imgbtn_dotIndicator:
                    boardDotFragment = new BoardDotFragment();
                    boardDotFragment.show(requireActivity().getSupportFragmentManager(), boardDotFragment.getTag());
                    break;
                // 새로 고침 버튼 클릭 시 불씨 그래픽 변화
                case R.id.imgBtn_refresh:
                    apiFire();
                    break;
                // 아래로 버튼 클릭 시 스크롤 변화
                case R.id.imgbtn_down:
                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.fullScroll(View.FOCUS_DOWN);
                            // 아래로 내려가면 안보이도록 설정
                            down.setVisibility(View.INVISIBLE);
                        }
                    });
                    break;
                /** 투표 버튼 클릭 시 **/
                case R.id.btn_vote:
                    Log.d(TAG, "투표 버튼 클릭");
                    noReadVote();
                    break;
                /** 공지 버튼 클릭 시 **/
                case R.id.btn_notice:
                    noReadNotice();
                    break;

                /** 공지 전체 보기 클릭 **/
                case R.id.btn_all:
                    Intent intent = new Intent(requireContext(), NoticeVoteActivity.class);
                    intent.putExtra("teamId", teamId);
                    // 공지
                    if(btnAll.getText().toString().contains("공지")) {
                        intent.putExtra("NoticeOrVote", 1);
                    }
                    // 투표
                    else {
                        intent.putExtra("NoticeOrVote", 2);
                    }
                    startActivity(intent);
                    break;
            }
        }
    }

    /**
     * 소모임 제목, 기간, 사진, 날짜 설정
     **/
    public void getApi() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, accessToken);
        apiService = RetrofitClientJwt.getApiService(accessToken);
        boardMoimResponseCall = apiService.moimInfo(accessToken, teamId);
        boardMoimResponseCall.enqueue(new Callback<BoardMoimResponse>() {
            @Override
            public void onResponse(Call<BoardMoimResponse> call, Response<BoardMoimResponse> response) {
                if(response.isSuccessful()) {
                    if(response.body() != null) {
                        BoardMoimResponse moimResponse = response.body();
                        String msg = moimResponse.getMessage();
                        if (msg.equals("목표보드 프로필을 조회하였습니다")) {
                            BoardMoimResponse.Data data = moimResponse.getData();
                            name = data.getName();
                            profileImg = data.getProfileImg();
                            remainPeriod = data.getRemainingPeriod();
                            Log.d(TAG, remainPeriod);
                            nowTime = data.getNowTime();

                            /** 데이터 확인 **/
                            Log.d("BOARDGOALFRAGMENT", name);
                            Log.d("BOARDGOALFRAGMENT", profileImg);
                            Log.d("BOARDGOALFRAGMENT", remainPeriod);
                            Log.d("BOARDGOALFRAGMENT", nowTime);

                            // 소모임 이름 설정
                            teamName.setText(name);

                            // S3 이미지 다운로드 -> 이미지 뷰에 설정
                            // 소모임 사진 설정
                            S3Utils.downloadImageFromS3(profileImg, new DownloadImageCallback() {
                                @Override
                                public void onImageDownloaded(byte[] data) {
                                    if(context != null) {
                                        runOnUiThread(() -> {
                                            glideManager = Glide.with(context);

                                            glideManager
                                                    .asBitmap()
                                                    .load(data)
                                                    .centerCrop()
                                                    .transform(new RoundedCorners(24))
                                                    .into(teamImg);

                                        });
                                    }

                                }

                                @Override
                                public void onImageDownloadFailed() {

                                }
                            });

                            // D-Day 설정
                            teamDay.setText("종료 " + remainPeriod);
                            // nowTime 설정
                            curDate.setText(nowTime);
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
                            ChangeJwt.updateJwtToken(getContext());
                            getApi();
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
            public void onFailure(Call<BoardMoimResponse> call, Throwable t) {
                Log.d("BoardGoalFragment", "만료된 토큰 - 연동 실패..");
            }
        });
    }

    /**
     * 불씨 (그래픽) 변화 API 통신
     **/
    public void apiFire() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        boardFireResponseCall = apiService.newBoardFire(accessToken, teamId);
        boardFireResponseCall.enqueue(new Callback<BoardFireResponse>() {
            @Override
            public void onResponse(Call<BoardFireResponse> call, Response<BoardFireResponse> response) {
                if(response.isSuccessful()) {
                    BoardFireResponse fireResponse = response.body();
                    String msg = fireResponse.getMessage();

                    Log.d(TAG, msg);

                    if (msg.equals("개인별 개인별 미션 인증 현황 조회 성공")) {
                        BoardFireResponse.Data data = fireResponse.getData();
                        checkFire(data.getPercent());
                        Log.d(TAG, data.getFireCopy());
                        tv_hot.setText(data.getFireCopy());

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
                            ChangeJwt.updateJwtToken(getContext());
                            apiFire();
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
            public void onFailure(Call<BoardFireResponse> call, Throwable t) {
                Log.d(TAG, "불씨 변화 실패...");
            }
        });
    }

    /**
     * fire Data에 따른 상태 변화 메서드
     **/
    public void checkFire(Long num) {
        if (num <= 19) {
            // 배경색 변화
            Drawable d = ContextCompat.getDrawable(requireContext(), R.drawable.ic_board_fire1_3x);
            fire.setBackground(d);
        } else if (num >= 20 && num <= 49) {
            Drawable d = ContextCompat.getDrawable(requireContext(), R.drawable.ic_board_fire2_3x);
            fire.setBackground(d);
        } else if (num >= 50 && num <= 79) {
            Drawable d = ContextCompat.getDrawable(requireContext(), R.drawable.ic_board_fire3_3x);
            fire.setBackground(d);
        } else {
//            Drawable d = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fire_motion);
//            fire.setBackground(d);
            Glide.with(this).load(R.drawable.ic_fire_motion).override(Target.SIZE_ORIGINAL).into(fire);
        }
    }

    /**
     * 공지 안 읽은 것만 조회
     **/
    public void noReadNotice() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        boardNoReadNoticeResponseCall = apiService.noReadNotice(accessToken, teamId);
        boardNoReadNoticeResponseCall.enqueue(new Callback<BoardNoReadNoticeResponse>() {
            @Override
            public void onResponse(Call<BoardNoReadNoticeResponse> call, Response<BoardNoReadNoticeResponse> response) {
                if(response.isSuccessful()) {
                    BoardNoReadNoticeResponse noReadResponse = response.body();
                    String msg = noReadResponse.getMessage();
                    if (msg.equals("확인하지 않은 공지를 최신순으로 조회하였습니다")) {
                        // 리스트 저장
                        noticeDataList = noReadResponse.getData();
                        boardGoalAdapter = new BoardGoalAdapter<BoardNoReadNoticeResponse.NoticeData>
                                (noticeDataList, BoardGoalFragment.this);
                        recyclerView.setAdapter(boardGoalAdapter);
                        boardGoalAdapter.notifyDataSetChanged();

                        List<Long> noNoticeList = new ArrayList<>();
                        for (BoardNoReadNoticeResponse.NoticeData noticeData : noticeDataList) {
                            noNoticeList.add(noticeData.getNoticeId());
                        }

                        /** 리사이클러뷰 아이템 클릭 이벤트 처리 **/
                        boardGoalAdapter.setOnItemClickListener(new BoardGoalAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int pos) {
                                noticeId = noNoticeList.get(pos);
                                Intent intent = new Intent(getActivity(), NoticeInfoActivity.class);
                                intent.putExtra("teamId", teamId);
                                intent.putExtra("noticeId", noticeId);
                                intent.putExtra("acitivityTask", 1);
                                startActivity(intent);
                            }
                        });

                        // 안 읽은 공지 개수 저장
                        noReadNotice = noticeDataList.size();
                        checkNoRead(noReadNotice, "공지");
                        btnAll.setText("공지 전체보기");

                        Log.d(TAG, "noreadNotice 통신 중 : " + noReadNotice);
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
                            ChangeJwt.updateJwtToken(getContext());
                            noReadNotice();
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
            public void onFailure(Call<BoardNoReadNoticeResponse> call, Throwable t) {
                Log.d(TAG, "최신순 공지 조회 연동 실패 ...");
            }
        });
    }

    /**
     * 투표 안 읽은 것만 조회
     **/
    public void noReadVote() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        boardNoReadVoteResponseCall = apiService.noReadVote(accessToken, teamId);
        boardNoReadVoteResponseCall.enqueue(new Callback<BoardNoReadVoteResponse>() {
            @Override
            public void onResponse(Call<BoardNoReadVoteResponse> call, Response<BoardNoReadVoteResponse> response) {
                if(response.isSuccessful()) {
                    BoardNoReadVoteResponse voteResponse = response.body();
                    String msg = voteResponse.getMessage();
                    if (msg.equals("확인하지 않은 투표를  최신순으로 조회하였습니다")) {
                        voteDataList = voteResponse.getData();
                        noReadVote = voteDataList.size();

                        boardGoalAdapter = new BoardGoalAdapter<BoardNoReadVoteResponse.VoteData>(voteDataList, BoardGoalFragment.this);
                        recyclerView.setAdapter(boardGoalAdapter);
                        boardGoalAdapter.notifyDataSetChanged();

                        List<Long> noVoteList = new ArrayList<>();
                        for (BoardNoReadVoteResponse.VoteData voteData : voteDataList) {
                            noVoteList.add(voteData.getVoteId());
                        }

                        /** 리사이클러뷰 아이템 클릭 이벤트 처리 **/
                        boardGoalAdapter.setOnItemClickListener(new BoardGoalAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int pos) {
                                // 아이템 클릭 이벤트 처리
                                voteId = noVoteList.get(pos);
                                Intent intent = new Intent(getActivity(), NoticeInfoActivity.class);
                                intent.putExtra("teamId", teamId);
                                intent.putExtra("voteId", voteId);
                                Log.d(TAG, "teamId :" + teamId +", voteID : " + voteId);
                                intent.putExtra("acitivityTask", 1);
                                startActivity(intent);
                            }
                        });

                        checkNoRead(noReadVote, "투표");
                        btnAll.setText("투표 전체보기");
                        Log.d(TAG, "noReadVote : " + noReadVote);

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
                            ChangeJwt.updateJwtToken(getContext());
                            noReadVote();
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
            public void onFailure(Call<BoardNoReadVoteResponse> call, Throwable t) {
                Log.d(TAG, "안 읽은 투표 최신순 연동 실패 ...");
            }
        });
    }


    /**
     * 확인하지 않은 공지/투표가 "3개" 있어요 문장에서 "3개"만 색깔 변경 처리
     **/
    public void checkNoRead(int num, String str) {
        String text = str.equals("공지") ? "공지" : "투표";
        Log.d(TAG, "checkNoRead의 text : " + text);
        String result = "확인하지 않은 " + text + "가 " + num + "개 있어요";
        // 확인하지 않은 공지가 "3개" 있어요 할때 "3개"만 글자 색상 변경
        SpannableString sp = new SpannableString(result); // 객체 생성
        String word = num + "개";
        int start = result.indexOf(word);
        int end = start + word.length();
        sp.setSpan(new ForegroundColorSpan(Color.parseColor("#FF725F")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_toggle.setText(sp);

        ColorStateList co = ColorStateList.valueOf(requireContext().getResources().getColor(R.color.secondary_grey_black_1));
        ColorStateList co2 = ColorStateList.valueOf(requireContext().getResources().getColor(R.color.secondary_grey_black_13));

        // List의 개수가 0개일 때
        if (num ==0) {
            recyclerView.setVisibility(View.GONE);
            String allCheck = "모든 " + text + "를 다 확인했어요";
            tv_all.setText(allCheck);
            tv_all.setVisibility(View.VISIBLE);
            tv_all2.setVisibility(View.VISIBLE);

            if(str.equals("공지")) {
                notice_sign.setVisibility(View.INVISIBLE);
                btn_notice.setBackgroundTintList(co);
                btn_vote.setBackgroundTintList(co2);
            }
            else {
                vote_sign.setVisibility(View.INVISIBLE);
                btn_notice.setBackgroundTintList(co2);
                btn_vote.setBackgroundTintList(co);
            }
        }

        // List의 개수가 한 개 이상일 때
        else {
            recyclerView.setVisibility(View.VISIBLE);
            tv_all.setVisibility(View.GONE);
            tv_all2.setVisibility(View.GONE);

            if(str.equals("공지")) {
                notice_sign.setVisibility(View.VISIBLE);
                btn_notice.setBackgroundTintList(co);
                btn_vote.setBackgroundTintList(co2);
            }
            else {
                vote_sign.setVisibility(View.VISIBLE);
                btn_notice.setBackgroundTintList(co2);
                btn_vote.setBackgroundTintList(co);
            }
        }
    }

    /** 현재 팀, 나의 진행도 구하는 API **/
    private void curLocation() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        boardCurrentLocateResponseCall = apiService.curLocate(accessToken, teamId);
        boardCurrentLocateResponseCall.enqueue(new Callback<BoardCurrentLocateResponse>() {
            @Override
            public void onResponse(Call<BoardCurrentLocateResponse> call, Response<BoardCurrentLocateResponse> response) {
                BoardCurrentLocateResponse locateResponse = response.body();
                if(response.isSuccessful()) {
                    BoardCurrentLocateResponse.Data data = locateResponse.getData();
                    personalRate = data.getPersonalRate();
                    teamRate = data.getTeamRate();
                    computeLocate(personalRate, teamRate);

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
                            ChangeJwt.updateJwtToken(getContext());
                            curLocation();
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
            public void onFailure(Call<BoardCurrentLocateResponse> call, Throwable t) {
                Log.d(TAG, "팀, 나의 퍼센트 연동 실패.." + t.getMessage());
            }
        });

    }

    /** 현재 위치 비율 계산 메서드 **/
    private void computeLocate(Long personalRate, Long teamRate) {
        // 가로 길이 구하기
        // 가로 길이 구하기
        int parentWidth = relative_progress.getWidth();

        Long myRate = personalRate;
        Long allRate = teamRate;
        if (myRate > 100) {
            myRate = Long.valueOf(100);
        }
        if (allRate > 100)
            allRate = Long.valueOf(100);

        // 퍼센테이지 계산
        float leftMarginTeamPercent = (float) allRate / 100;
        float leftMarginMyPercent = (float) myRate / 100;

        // ImageView의 왼쪽 여백 계산
        int teamLeftMargin = (int) (parentWidth * leftMarginTeamPercent);
        int myLeftMargin = (int) (parentWidth * leftMarginMyPercent);
        // px를 dp로 변환
        int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, getResources().getDisplayMetrics());
        int marginTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        /** 내 미션 상황 이미지 **/
        // 기존의 ImageView의 LayoutParams 가져오기
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imgUser.getLayoutParams();
        // ImageView의 레이아웃 파라미터 값 설정
        params.width = width;
        params.height = width;
        params.topMargin = marginTop;
        if (myRate == 0) {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (myRate == 100) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.rightMargin = marginTop;
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.setMarginStart(myLeftMargin);
        }

        /** Team **/
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) imgTeam.getLayoutParams();
        params2.width = width;
        params2.height = width;
        params2.topMargin = marginTop;
        if (allRate == 0)
            params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        else if (allRate == 100) {
            params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params2.rightMargin = marginTop;
        }
        else {
            params2.addRule(RelativeLayout.ALIGN_PARENT_START);
            params2.setMarginStart(teamLeftMargin);
        }
        // personalRate에 따라 왼쪽 여백 설정

        // TextView 위치 설정
        /** TextTeam **/
        RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) tv_progress_team.getLayoutParams();
        params3.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        params3.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        if (allRate == 0)
            params3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        else if (allRate == 100) {
            params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params3.rightMargin = marginTop;
        }
        else {
            params3.addRule(RelativeLayout.ALIGN_PARENT_START);
            params3.setMarginStart(teamLeftMargin - 60);
        }

        /** TextUser **/
        RelativeLayout.LayoutParams params4 = (RelativeLayout.LayoutParams) tv_progress_user.getLayoutParams();
        params4.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        params4.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        if (myRate == 0)
            params4.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        else if (myRate == 100) {
            params4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params4.rightMargin = marginTop;
        }
        else {
            params4.addRule(RelativeLayout.ALIGN_PARENT_START);
            params4.setMarginStart(myLeftMargin);
        }

        // ImageView에 레이아웃 파라미터 설정
        imgUser.setLayoutParams(params);
        imgTeam.setLayoutParams(params2);
        tv_progress_team.setLayoutParams(params3);
        tv_progress_user.setLayoutParams(params4);
    }
}