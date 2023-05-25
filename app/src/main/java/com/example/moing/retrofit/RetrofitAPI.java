package com.example.moing.retrofit;

import com.example.moing.Request.AlarmRequest;
import com.example.moing.Request.BoardMakeVoteRequest;
import com.example.moing.Request.BoardVoteDoRequest;
import com.example.moing.Request.BoardVoteMakeCommentRequest;
import com.example.moing.Request.ChangeJwtRequest;
import com.example.moing.Request.LoginRequest;
import com.example.moing.Request.MakeTeamRequest;
import com.example.moing.Request.MissionCreateRequest;
import com.example.moing.Request.MissionUpdateRequest;
import com.example.moing.Request.NoticeCommentRequest;
import com.example.moing.Request.NoticeCreateRequest;
import com.example.moing.Request.ProfileUpdateRequest;
import com.example.moing.Request.RegisterAddressRequest;
import com.example.moing.Request.TeamUpdateRequest;
import com.example.moing.Response.AlarmResponse;
import com.example.moing.Response.AlarmSettingResponse;
import com.example.moing.Response.AllVoteResponse;
import com.example.moing.Response.BoardCurrentLocateResponse;
import com.example.moing.Response.BoardFireResponse;
import com.example.moing.Response.BoardMakeVoteResponse;
import com.example.moing.Response.BoardMoimResponse;
import com.example.moing.Response.BoardNoReadNoticeResponse;
import com.example.moing.Response.BoardNoReadVoteResponse;
import com.example.moing.Response.BoardVoteCommentResponse;
import com.example.moing.Response.MoimMasterResponse;
import com.example.moing.Response.NoticeVoteFinishResponse;
import com.example.moing.Response.BoardVoteInfoResponse;
import com.example.moing.Response.BoardVoteMakeCommentResponse;
import com.example.moing.Response.ChangeJwtResponse;
import com.example.moing.Response.CheckAdditionalInfo;
import com.example.moing.Response.InvitationCodeResponse;
import com.example.moing.Response.InviteTeamResponse;
import com.example.moing.Response.LoginResponse;
import com.example.moing.Response.MakeTeamResponse;
import com.example.moing.Response.AllNoticeResponse;
import com.example.moing.Response.MissionCreateResponse;
import com.example.moing.Response.MissionListResponse;
import com.example.moing.Response.MissionUpdateResponse;
import com.example.moing.Response.MyPageResponse;
import com.example.moing.Response.NoticeCommentListResponse;
import com.example.moing.Response.NoticeCommentResponse;
import com.example.moing.Response.NoticeCreateResponse;
import com.example.moing.Response.NoticeInfoResponse;
import com.example.moing.Response.MissionClearResponse;
import com.example.moing.Response.MissionInfoResponse;
import com.example.moing.Response.MissionSkipResponse;
import com.example.moing.Response.MissionStatusListResponse;
import com.example.moing.Response.ProfileUpdateResponse;
import com.example.moing.Response.RegisterAddressResponse;
import com.example.moing.Response.RegisterNameResponse;
import com.example.moing.Response.TeamListResponse;
import com.example.moing.Response.TeamResponse;
import com.example.moing.Response.TeamUpdateResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** REST API와 통신하기 위한 Interface **/
public interface RetrofitAPI {
    //@POST("/api/v1/team/join")
    @POST("/api/v1/users/auth/kakao")
    Call<LoginResponse> kakaoLogin(@Body LoginRequest loginRequest);

    @GET("/api/v1/users/nickname/{nickname}/available")
    Call<RegisterNameResponse> NameAvailable(@Path("nickname") String nickname);

    @Headers({"Content-Type: application/json"})
    @POST("/api/v1/users/additional-info")
    Call<RegisterAddressResponse> AdditionalInfo(@Header("Authorization") String token, @Body RegisterAddressRequest registerAddressRequest);

    /** 소모임 생성 **/
    @POST("/api/v1/team")
    Call<MakeTeamResponse> makeTeam(@Header("Authorization") String token, @Body MakeTeamRequest makeTeamRequest);

    /** Jwt 갱신 **/
    @POST("/api/v1/users/auth/refresh")
    Call<ChangeJwtResponse> changeJwt(@Body ChangeJwtRequest changeJwtRequest);

    /** 추가정보 입력여부 확인 **/
    @GET("/api/v1/users/additional-info")
    Call<CheckAdditionalInfo> checkAdditionalInfo(@Header("Authorization") String token);

    /** 목표보드 소모임 정보 보기 **/
    @GET("/api/v1/team/{teamId}/goal-board")
    Call<BoardMoimResponse> moimInfo(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 목표보드 소모임 불 새로고침 **/
    @GET("/api/v1/{teamId}/missions/teamRate")
    Call<BoardFireResponse> newBoardFire(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 목표보드 팀위치, 현재위치 **/
    @GET("/api/v1/{teamId}/missions/graph")
    Call<BoardCurrentLocateResponse> curLocate(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 홈 화면 소모임 목록 GET **/
    @GET("/api/v1/team")
    Call<TeamListResponse> getTeamList(@Header("Authorization") String token);

    /** 공지 안 읽은 것만 조회 **/
    @GET("/api/v1/{teamId}/notice/unread")
    Call<BoardNoReadNoticeResponse> noReadNotice(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 투표 안 읽은 것만 조회 **/
    @GET("/api/v1/{teamId}/vote/unread")
    Call<BoardNoReadVoteResponse> noReadVote(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 공지 전체 조회 **/
    @GET("/api/v1/{teamId}/notice")
    Call<AllNoticeResponse> viewNotice(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 투표 전체 조회 **/
    @GET("/api/v1/{teamId}/vote")
    Call<AllVoteResponse> viewVote(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 투표 생성 **/
    @POST("/api/v1/{teamId}/vote")
    Call<BoardMakeVoteResponse> makeVote(@Header("Authorization") String token, @Path("teamId") Long teamId, @Body BoardMakeVoteRequest boardMakeVoteRequest);

    /** 미션 리스트 조회 **/
    @GET("api/v1/{teamId}/missions")
    Call<MissionListResponse> MissionList(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 미션 생성 **/
    @POST("api/v1/{teamId}/missions")
    Call<MissionCreateResponse> makeMission(@Header("Authorization") String token, @Path("teamId") Long teamId, @Body MissionCreateRequest missionCreateRequest);

    /** 공지 댓글 생성 **/
    @POST("/api/v1/{teamId}/notice/{noticeId}/comment")
    Call<NoticeCommentResponse> makeNoticeComment(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("noticeId") Long noticeId, @Body NoticeCommentRequest noticeCommentRequest);

    /** 공지 댓글 삭제 **/
    @DELETE("/api/v1/{teamId}/notice/{noticeId}/comment/{commentId}")
    Call<NoticeVoteFinishResponse> deleteNoticeComment(@Header("Authorization") String token,
                                                     @Path("teamId") Long teamId, @Path("noticeId") Long noticeId,
                                                     @Path("commentId") Long commentId);

    /** 공지 댓글 목록 조회 **/
    @GET("/api/v1/{teamId}/notice/{noticeId}/comment")
    Call<NoticeCommentListResponse> makeNoticeCommentList(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("noticeId") Long noticeId);

    /** 공지 생성 **/
    @POST("/api/v1/{teamId}/notice")
    Call<NoticeCreateResponse> makeNotice(@Header("Authorization") String token, @Path("teamId") Long teamId, @Body NoticeCreateRequest noticeCreateRequest);

    /** 공지 상세 조회 **/
    @GET("/api/v1/{teamId}/notice/{noticeId}")
    Call<NoticeInfoResponse> NoticeInfo(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("noticeId") Long noticeId);

    /** 투표 결과 상세(하나만) 조회 **/
    @GET("/api/v1/{teamId}/vote/{voteId}")
    Call<BoardVoteInfoResponse> voteDetailInfo(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("voteId") Long voteId);

    /** 투표하기 **/
    @PUT("/api/v1/{teamId}/vote/{voteId}")
    Call<BoardVoteInfoResponse> voteResult(@Header("Authorization") String token,
                                           @Path("teamId") Long teamId, @Path("voteId") Long voteId,
                                           @Body BoardVoteDoRequest boardVoteDoRequest);

    /**
     * 투표 댓글 목록 조회
     **/
    @GET("/api/v1/{teamId}/vote/{voteId}/comment")
    Call<BoardVoteCommentResponse> voteCommentInfo(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("voteId") Long voteId);

    /** 투표 종료 **/
    @DELETE("/api/v1/{teamId}/vote/{voteId}")
    Call<NoticeVoteFinishResponse> deleteVote(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("voteId") Long voteId);

    /** 공지 종료 **/
    @DELETE("/api/v1/{teamId}/notice/{noticeId}")
    Call<NoticeVoteFinishResponse> deleteNotice(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("noticeId") Long noticeId);
    /**
     * 투표 댓글 생성
     **/
    @POST("/api/v1/{teamId}/vote/{voteId}/comment")
    Call<BoardVoteMakeCommentResponse> voteMakeComment(@Header("Authorization") String token, @Path("teamId") Long teamId,
                                                       @Path("voteId") Long voteId, @Body BoardVoteMakeCommentRequest request);

    /** 투표 댓글 삭제 **/
    @DELETE("/api/v1/{teamId}/vote/{voteId}/comment/{commentId}")
    Call<NoticeVoteFinishResponse> deleteVoteComment(@Header("Authorization") String token,
                                                     @Path("teamId") Long teamId, @Path("voteId") Long voteId,
                                                     @Path("commentId") Long commentId);

    /** 미션 인증 **/
    @POST("api/v1/{teamId}/missions/{missionId}/submit")
    Call<MissionClearResponse> missionClear(@Header("Authorization") String token,
                                            @Path("teamId") Long teamId, @Path("missionId") Long missionId, @Body String url);

    /** 미션 한 개 상세 조회 **/
    @GET("api/v1/{teamId}/missions/{missionId}")
    Call<MissionInfoResponse> getMission(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("missionId") Long missionId);

    /** 미션 건너뛰기 **/
    @POST("api/v1/{teamId}/missions/{missionId}/skip")
    Call<MissionSkipResponse> skipMyMission(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("missionId") Long missionId, @Body String reason);

    /** 소모임 정보 조회 (수정을 위한 정보 조회) **/
    @GET("/api/v1/team/{teamId}")
    Call<TeamResponse> getTeam(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 소모임 초대 코드 조회  **/
    @GET("/api/v1/team/{teamId}/invite-code")
    Call<InvitationCodeResponse> getInvitationCode(@Header("Authorization") String token, @Path("teamId") Long teamId);

    /** 소모임 정보 수정  **/
    @PUT("/api/v1/team/{teamId}")
    Call<TeamUpdateResponse> putTeamUpdate(@Header("Authorization") String token, @Path("teamId") Long teamId, @Body TeamUpdateRequest teamUpdateRequest);

    /** 초대 코드를 통한 소모임 참여 **/
    @POST("/api/v1/team/join")
    Call<InviteTeamResponse> postAuthInvitationCode(@Header("Authorization") String token, @Body String invitationCode);

    /** 미션 인증 현황들 조회 **/
    @GET("/api/v1/{teamId}/missions/{missionId}/status")
    Call<MissionStatusListResponse> getMissionStatusList(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("missionId") Long missionId);

    /** 불 던지기 **/
    @POST("api/v1/{teamId}/missions/{missionId}/fire/{usermissionId}")
    Call<String> postThrowFire(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("missionId") Long missionId, @Path("usermissionId") Long usermissionId);

    /** 마이페이지 조회 **/
    @GET("/api/v1/users/mypage")
    Call<MyPageResponse> getMyPage(@Header("Authorization") String token);

    /** 알림 설정 상태 조회 **/
    @GET("/api/v1/users/alarm-setting")
    Call<AlarmSettingResponse> getAlarmSetting(@Header("Authorization") String token);

    /** 신규 업로드 알림 상태 설정 **/
    @PUT("/api/v1/users/alarm-setting/new-upload")
    Call<AlarmResponse> putAlarmNew(@Header("Authorization") String token, @Body AlarmRequest request);

    /** 미션 리마인드 알림 상태 설정 **/
    @PUT("/api/v1/users/alarm-setting/remind")
    Call<AlarmResponse> putAlarmRemind(@Header("Authorization") String token, @Body AlarmRequest request);

    /** 불 던지기 알림 상태 설정 **/
    @PUT("/api/v1/users/alarm-setting/fire")
    Call<AlarmResponse> putAlarmFire(@Header("Authorization") String token,@Body AlarmRequest request);

    /** 프로필 수정 **/
    @PUT("/api/v1/users/mypage")
    Call<ProfileUpdateResponse> putProfileUpdate(@Header("Authorization") String token, @Body ProfileUpdateRequest request);

    /** 미션 정보 수정  **/
    @PUT("api/v1/{teamId}/missions/{missionId}")
    Call<MissionUpdateResponse> putMissionUpdate(@Header("Authorization") String token, @Path("teamId") Long teamId, @Path("missionId") Long missionId, @Body MissionUpdateRequest missionUpdateRequest);

    /** 리더 상태 조회 **/
    @GET("/api/v1/{teamId}/missions/isLeader")
    Call<MoimMasterResponse> checkIsMoimMaster(@Header("Authorization") String token, @Path("teamId") Long teamId);
}
