package com.example.moing.mission;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moing.R;
import com.example.moing.response.MissionStatusListResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionStatusUndoneFragment extends Fragment {
    private static final String TAG = "MissionStatusUndoneFragment";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private Dialog undoneDialog;
    private ArrayList<MissionStatusListResponse.UserMission> undoneList;
    private ArrayList<Integer> fireList;
    private MissionUndoneAdapter adapter;

    // 더미 - 삭제 예정
    private long teamId;
    private long missionId;
    private boolean isExist;

    private Call<String> call;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mission_status_undone, container, false);

        // teamId, missionId 받아오기
        teamId = getArguments().getLong("teamId");
        missionId = getArguments().getLong("missionId");

        // MissionStatusActivity에서 전달받은 인증 미완료 리스트
        Bundle bundle = getArguments();
        undoneList = (ArrayList<MissionStatusListResponse.UserMission>) (bundle != null ? bundle.getSerializable("undoneList") : null);

        // MissionStatusActivity에서 전달받은 인증 완료 리스트
        fireList = (ArrayList<Integer>) (bundle != null ? bundle.getSerializable("fireList") : null);

        // "나"를 표현하기 위한 나의 상태
        isExist = bundle.getBoolean("isExist");

        // 리사이클러뷰
        RecyclerView recyclerView = rootView.findViewById(R.id.mission_status_undone_rv);
        setupRecyclerView(recyclerView);

        // 미완료 인원 수 설정
        TextView tvNum = rootView.findViewById(R.id.mission_status_undone_tv_num);
        int size = undoneList != null ? undoneList.size() : 0;
        String text = size+"명";
        tvNum.setText(text);

        // 미완료 0명 - 모두 완료 했을 경우 이미지
        ImageView ivDoneAll = rootView.findViewById(R.id.mission_status_undone_iv_done_all);
        if(size == 0)
            ivDoneAll.setVisibility(View.VISIBLE);
        else
            ivDoneAll.setVisibility(View.GONE);

        // 미션 건너뛰기 다이얼로그
        undoneDialog = new Dialog(getContext());
        undoneDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        undoneDialog.setContentView(R.layout.fragment_mission_status_undone_popup); // xml 레이아웃 파일 연결

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(call != null){
            call.cancel();
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        // 리사이클러뷰에 사용할 레이아웃매니저 - Grid 2개의 열
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        // 리사이클러뷰 아이템들 간 간격 설정
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 36, 0, 0);
            }
        });

        // 리사이클러뷰에 사용할 어댑터 설정
        adapter = new MissionUndoneAdapter(undoneList, fireList, getContext(),isExist);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this::showUndoneDialog);
    }


    // 미완료 상태 다이얼로그 띄우기 - 불 던지기 다이얼로그
    private void showUndoneDialog(MissionUndoneAdapter.UndoneViewHolder holder, int position, MissionStatusListResponse.UserMission mission){
        undoneDialog.show(); // 다이얼로그 띄우기
        undoneDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 투명 배경

        ImageButton btnClose = undoneDialog.findViewById(R.id.mission_status_undone_popup_btn_close);
        ImageButton btnThrow = undoneDialog.findViewById(R.id.mission_status_undone_popup_btn_throw);
        TextView tvThrow = undoneDialog.findViewById(R.id.mission_status_undone_popup_tv_throw);

        // 누가 누구에게 던지는지 표시
        String text = mission.getNickname()+"님이 아직 미션을 완료하지 못했네요!\n" + mission.getNickname()+"님의 발등에 불을 떨어트려볼까요?";
        tvThrow.setText(text);

        // 창 닫기
        btnClose.setOnClickListener(v -> undoneDialog.dismiss());

        // 불 던지기
        btnThrow.setOnClickListener(v -> {
            // 맞은 사람 표시
            holder.ivProfile.setColorFilter(Color.parseColor("#2C0E0E9C"));
            holder.ivFire.setVisibility(View.VISIBLE);

            // 불 맞은 리스트에 해당 아이템 추가
            fireList.add(mission.getUserMissionId());

            // fireList 업데이트
            adapter.setFireList(fireList);

            // 클릭 못하게 설정
            holder.itemView.setClickable(false);

            // 불 던지기 api 통신
            postThrowFire(mission.getUserMissionId());

            showCustomToast(mission.getNickname());

            undoneDialog.dismiss();
        });
    }

    private void postThrowFire(long usermissionId) {
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        call = apiService.postThrowFire(jwtAccessToken,teamId,missionId,usermissionId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull Call<String> call, @androidx.annotation.NonNull Response<String> response) {
                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, response.message());
                    }
                } else  {
                    Log.d(TAG, response.message());
                    try {
                        String errorJson = response.errorBody().string();
                        JSONObject errorObject = new JSONObject(errorJson);
                        // 에러 코드로 에러처리를 하고 싶을 때
                        // String errorCode = errorObject.getString("errorCode");
                        /** 메세지로 에러처리를 구분 **/
                        String message = errorObject.getString("message");

                        if (message.equals("만료된 토큰입니다.")) {
                            // 토큰 재발급 후 다시 호출
                            ChangeJwt.updateJwtToken(getContext());
                            postThrowFire(usermissionId);
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
            public void onFailure(@androidx.annotation.NonNull Call<String> call, @androidx.annotation.NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "postThrowFire Fail");
            }
        });
    }

    // 커스텀 Toast 메시지 표시
    public void showCustomToast(String nickName) {
        // 커스텀 레이아웃 파일 인플레이션
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custon_toast_throw_fire, null);
        TextView tvNickName = layout.findViewById(R.id.custom_toast_tv_nickname);
        tvNickName.setText(nickName);

        // Toast 객체 생성 및 커스텀 레이아웃 설정
        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        // Toast 메시지 표시
        toast.show();
    }
}