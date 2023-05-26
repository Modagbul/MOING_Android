package com.example.moing.board;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.moing.R;
import com.example.moing.Response.MoimMasterResponse;
import com.example.moing.mission.MissionCreateActivity;
import com.example.moing.mission.MissionListAdapter;
import com.example.moing.R;
import com.example.moing.response.MissionListResponse;
import com.example.moing.mission.MissionClickActivity;
import com.example.moing.mypage.MyPageTeamAdatper;
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

public class BoardMissionFragment extends Fragment {

    private RetrofitAPI apiService;
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private SharedPreferences sharedPreferences;
    private Dialog inviteDialogImpossible;

    // RecyclerView
    private RecyclerView mRecyclerView;
    private List<MissionListResponse.MissionData> missionList;  // 추가된 부분

    Long teamId;

    TextView et_content;
    ImageView createBtn;

    private Call<MissionListResponse> call;
    private Call<MoimMasterResponse> call2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_board_mission, container, false);

        // Token을 사용할 SharedPreference
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Intent로 값 받기
        teamId = getActivity().getIntent().getLongExtra("teamId", 0);
        Log.d(TAG, "teamId 값 : " + String.valueOf(teamId));

        et_content = view.findViewById(R.id.et_content);

        mRecyclerView = view.findViewById(R.id.recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        createBtn = view.findViewById(R.id.mission_create_btn);
//        createBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(requireContext(), MissionCreateActivity.class);
//                intent.putExtra("teamId", teamId);
//                startActivity(intent);
//            }
//        });


        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 임시로 가정된 API와의 통신하여 소모임장 여부 확인
                checkIsMoimMaster();
            }
            });

        // 초대 코드 팝업 다이얼로그 - 불가능
        inviteDialogImpossible = new Dialog(getContext());
        inviteDialogImpossible.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        inviteDialogImpossible.setContentView(R.layout.fragment_board_invite_code_impossible); // xml 레이아웃 파일 연결

        /** 공지사항 **/
        MissionList();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(call != null)
            call.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        MissionList();
    }

    private void checkIsMoimMaster() {
        // Token 을 가져오기 위한 SharedPreferences Token
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);


        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        call2 = apiService.checkIsMoimMaster(jwtAccessToken, teamId);

        call2.enqueue(new Callback<MoimMasterResponse>() {
            @Override
            public void onResponse(@NonNull Call<MoimMasterResponse> call, @NonNull Response<MoimMasterResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        MoimMasterResponse masterResponse = response.body();
                        if (masterResponse.isMoimMaster()) {
                            // 소모임장인 경우
                            Intent intent = new Intent(requireContext(), MissionCreateActivity.class);
                            intent.putExtra("teamId", teamId);
                            startActivity(intent);
                        } else {
                            // 소모임장이 아닌 경우
                            showInviteDialogImpossible();
                        }
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

                        if (message.equals("소모임장이 아니어서 할 수 없습니다.")) {
                            showInviteDialogImpossible();
                        } else if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getContext());
                            checkIsMoimMaster();
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
            public void onFailure(@NonNull Call<MoimMasterResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "마이페이지 조회 실패");
            }
        });
    }

        /**
         * 미션 리스트 출력 API
         **/
    public void MissionList() {
        String accessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null); // 액세스 토큰 검색
        apiService = RetrofitClientJwt.getApiService(accessToken);

        call = apiService.MissionList(accessToken, teamId);
        call.enqueue(new Callback<MissionListResponse>() {
            @Override
            public void onResponse(Call<MissionListResponse> call, Response<MissionListResponse> response) {
                if(response.isSuccessful()) {
                    MissionListResponse missionListResponse = response.body();
                    String msg = missionListResponse.getMessage();
                    if (msg.equals("개인별 미션 리스트 조회 성공")) {
                        // 리스트 저장

                        et_content.setVisibility(View.INVISIBLE);

                        missionList = missionListResponse.getData();
                        List<Long> missionIdList = new ArrayList<>();
                        for (MissionListResponse.MissionData mission : missionList) {
                            missionIdList.add(mission.getMissionId());
                        }

                        for(Long str: missionIdList)
                            Log.d(TAG, "missionList 값: " + str);

                        MissionListAdapter adapter = new MissionListAdapter(missionList, getContext());
                        mRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        adapter.setOnItemClickListener(new MissionListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int pos) {
                                /** 해당 공지사항으로 이동 **/
                                Long missionId = missionIdList.get(pos);
                                Intent intent = new Intent(getContext(), MissionClickActivity.class);
                                intent.putExtra("teamId", teamId);
                                intent.putExtra("missionId", missionId);
                                startActivity(intent);

                            }
                        });
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

                        if(message.equals("소모임장이 아니어서 할 수 없습니다.")) {
                            showInviteDialogImpossible();
                        }
                        else if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getContext());
                            MissionList();
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
            public void onFailure(Call<MissionListResponse> call, Throwable t) {
                Log.d(TAG, "공지 전체 조회 실패...");
            }
        });

    }

    // 초대 코드 복사 불가 팝업
    private void showInviteDialogImpossible() {
        inviteDialogImpossible.show(); // 다이얼로그 띄우기
        inviteDialogImpossible.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 투명 배경

        ImageButton btnClose = inviteDialogImpossible.findViewById(R.id.board_invite_code_impossible_btn_close);
        View llWhole = inviteDialogImpossible.findViewById(R.id.board_invite_code_impossible_ll);

        // 터치시 종료
        llWhole.setOnClickListener(v -> inviteDialogImpossible.dismiss());
        btnClose.setOnClickListener(v -> inviteDialogImpossible.dismiss());
    }
}
