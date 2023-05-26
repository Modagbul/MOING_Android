package com.example.moing.board.notice;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moing.R;
import com.example.moing.response.NoticeVoteFinishResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeDeleteFragment extends BottomSheetDialogFragment {
    private static final String TAG = "NoticeDeleteFragment";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";

    private Dialog deleteDialog;
    private Long teamId, noticeId;
    private int activityTask;

    private Call<NoticeVoteFinishResponse> call;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_notice_delete, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            teamId = bundle.getLong("teamId");
            noticeId = bundle.getLong("noticeId");
            activityTask = bundle.getInt("activityTask");
        }

        Log.d(TAG, "noticeId 전달받은 값 : " + noticeId);

        // 닫기 버튼
        ImageButton btnClose = view.findViewById(R.id.board_dot_btn_close);
        btnClose.setOnClickListener(closeClickListener);

        // 공지사항 삭제 버튼
        ImageButton btnDelete = view.findViewById(R.id.board_dot_delete_notice);
        btnDelete.setOnClickListener(deleteClickListener);

        // 삭제 표시 다이얼로그
        deleteDialog = new Dialog(getContext());
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        deleteDialog.setContentView(R.layout.fragment_board_delete_notice_check);
       return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(call != null)
            call.cancel();
    }

    // 뒤로 가기
    View.OnClickListener closeClickListener = v -> dismiss();

    // 공지 삭제 팝업 띄우기
    View.OnClickListener deleteClickListener = v -> {
        ShowDelete();
    };

    // 공지 삭제 팝업
    private void ShowDelete() {
        TextView tv = deleteDialog.findViewById(R.id.tv_title);
        Button noDelete = deleteDialog.findViewById(R.id.btn_delete_no);
        Button yesDelete = deleteDialog.findViewById(R.id.btn_delete_yes);
        View viewAll = deleteDialog.findViewById(R.id.view_all);

        tv.setText("해당 공지를 정말 삭제하시겠습니까?");

        deleteDialog.show();
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 투명 배경

        // 창 닫기
        noDelete.setOnClickListener(v -> deleteDialog.dismiss());
        viewAll.setOnClickListener(v -> deleteDialog.dismiss());

        // 예 클릭 시
        yesDelete.setOnClickListener(v -> {
            deleteNoticeAPI();
        });
    }

    // 삭제 API
    private void deleteNoticeAPI() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);

        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        call = apiService.deleteNotice(jwtAccessToken, teamId, noticeId);
        call.enqueue(new Callback<NoticeVoteFinishResponse>() {
            @Override
            public void onResponse(Call<NoticeVoteFinishResponse> call, Response<NoticeVoteFinishResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, "테스트 1");
                        // 연결 성공 -> 삭제후 목록으로 이동!!!
                        deleteDialog.dismiss();

                        Intent intent = new Intent(getContext(), NoticeVoteActivity.class);
                        intent.putExtra("teamId", teamId);
                        // 목표보드에서 투표 상세로 바로 이동했을 때
                        if (activityTask != 1) {
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        }
                        // 투표 생성 후 투표 상세로 이동했거나, 투표 목록에서 투표 상세로 이동했을 때
                        startActivity(intent);

                        Toast.makeText(getActivity(), "공지가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
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

                        if(message.equals("해당 공지를 작성한 유저가 아닙니다")) {
                            Toast.makeText(getActivity(), "작성자만 공지를 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show();
                            deleteDialog.dismiss();
                        }
                        else if (message.equals("만료된 토큰입니다.")) {
                            ChangeJwt.updateJwtToken(getContext());
                            deleteNoticeAPI();
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
                Log.d(TAG, "테스트 4");
            }
        });
    }


}