package com.example.moing.board.vote;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moing.R;
import com.example.moing.response.BoardVoteInfoResponse;

import java.util.ArrayList;
import java.util.List;

public class VoteInfoAdapterFirst extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "VoteInfoAdapterFirst";
    private List<BoardVoteInfoResponse.VoteChoice> voteChoiceList;
    private List<BoardVoteInfoResponse.VoteChoice> voteSelected;
    private Context context;
    private OnItemClickListener clickListener = null;
    private boolean anonymous, multi, checkResult;

    // 리사이클러뷰 안의 리사이클러뷰 관련
    // 두번째 어댑터와 연결
    VoteInfoAdapterSecond second_adapter;

    // 클릭 리스너 설정
    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    // checkResult 변수는 결과를 가져올 때와, 투표를 선택했을 때를 구분하기 위해 집어넣은 변수 (false는 투표 전 결과, true는 투표 후 결과를 의미한다.)
    public VoteInfoAdapterFirst(List<BoardVoteInfoResponse.VoteChoice> voteChoiceList, List<BoardVoteInfoResponse.VoteChoice> voteSelected,
                                Context context, boolean checkResult) {
        this.voteChoiceList = voteChoiceList;
        this.voteSelected = voteSelected;
        this.context = context;
        this.checkResult = checkResult;
    }

    @Override
    public int getItemCount() {
        return voteChoiceList.size();
    }

    public List<BoardVoteInfoResponse.VoteChoice> getSelectedItems() {
        return voteSelected;
    }

    public void setMultiAnony(boolean val1, boolean val2) {
        multi = val1;
        anonymous = val2;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_item_vote_first, parent, false);
        return new VoteInfoAdapterFirst.VoteInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BoardVoteInfoResponse.VoteChoice voteChoice = voteChoiceList.get(position);
        VoteInfoViewHolder vH = (VoteInfoViewHolder) holder;
        vH.tv_text.setText(voteChoice.getContent());

        // 익명일 때 가시성 여부
        vH.btn_people.setVisibility(anonymous ? View.INVISIBLE : View.VISIBLE);

        if(checkResult) {
            // 제일 많이 투표한 항목 중에서
            if (voteChoice.getVoteUserNickName().size() == getMaxUserCount()) {
                vH.mView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.main_dark_500));
                vH.tv_text.setTextColor(ContextCompat.getColor(context, R.color.secondary_grey_black_1));
                vH.count.setTextColor(ContextCompat.getColor(context, R.color.secondary_grey_black_1));
            }
        }

        // Checkbox 상태 변경
        vH.checkBox.setChecked(voteChoice.isChecked());
        if (voteChoice.isChecked()) {
            vH.checkBox.setBackgroundResource(R.drawable.board_checkbox_yes);
            vH.checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#F6F6F6")));
        } else {
            vH.checkBox.setBackgroundResource(R.drawable.board_checkbox_no);
            // 선택되지 않은 상태의 배경색 제거
            vH.checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#959698")));
        }

        int spanCount = 4; // 열 개수를 의미

        /** 두번째 리싸이클러뷰 넣는 부분 **/
        GridLayoutManager llm2 = new GridLayoutManager(context, spanCount);
        llm2.setSmoothScrollbarEnabled(true);
        llm2.setAutoMeasureEnabled(true);

        // 안읽은 사람 리스트(GridLayout) 간격 설정
        int grid_spacing = (int) context.getResources().getDimension(R.dimen.grid_spacing);
        VoteNoReadGridSpacing voteNoReadGridSpacing = new VoteNoReadGridSpacing(4, grid_spacing);
        vH.recyclerView.addItemDecoration(voteNoReadGridSpacing);
        vH.recyclerView.setLayoutManager(llm2);

        /** 각 투표 리스트 별 사용자 항목을 구분해주기 위한 이중 리스트 생성 **/
        List<List<String>> userList2 = new ArrayList<>();
        // 2개 선택했다면 2번 반복
        for (BoardVoteInfoResponse.VoteChoice vc : voteChoiceList) {
            List<String> tmp = vc.getVoteUserNickName();
            Log.d(TAG, tmp.toString());
            userList2.add(tmp);
        }

        second_adapter = new VoteInfoAdapterSecond(voteChoiceList, voteChoice.getVoteUserNickName(), multi);
        vH.recyclerView.setAdapter(second_adapter);
        /** Second Adapter 전달 완료 **/

        /** 사람 수 받아와서 텍스트에 설정하기!! **/
        int num = voteChoice.getVoteUserNickName().size();
        vH.count.setText(String.valueOf(num) + "표");

        /** 버튼 클릭시 카드뷰(투표한 사람들)가 보임 **/
        vH.btn_people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = (vH.recyclerView.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
                TransitionManager.beginDelayedTransition((ViewGroup) vH.itemView, new AutoTransition());
                vH.recyclerView.setVisibility(visibility);
                // 보이는 상태라면
                if (visibility == View.VISIBLE)
                    vH.btn_people.setBackgroundResource(R.drawable.arrow_up);
                else
                    vH.btn_people.setBackgroundResource(R.drawable.arrow_down);
            }
        });
    }

    /**
     * ViewHolder 작성
     **/
    class VoteInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CheckBox checkBox;
        TextView tv_text, count;
        Button btn_people;
        RecyclerView recyclerView;
        View mView;

        public VoteInfoViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            checkBox = itemView.findViewById(R.id.btn_check);
            tv_text = itemView.findViewById(R.id.tv_item_date);
            count = itemView.findViewById(R.id.tv_voteCount);
            btn_people = itemView.findViewById(R.id.btn_voteCheck);
            recyclerView = itemView.findViewById(R.id.recycle_second);

            itemView.setOnClickListener(this);
            checkBox.setOnClickListener(this);
        }

        /**
         * 리싸이클러뷰 아이템 및 Checkbox 클릭을 동일하게 설정
         **/
        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                // 다중 선택이 아닌 경우
                if (!multi) {
                    // 이미 선택된 항목이 있다면 해제
                    for (BoardVoteInfoResponse.VoteChoice choice : voteSelected) {
                        if (choice.isChecked()) {
                            choice.setChecked(false);
                            // 선택된 CheckBox 해제
                            notifyDataSetChanged(); // 아이템 변경을 알림
                            break;
                        }
                    }
                    voteSelected.clear();
                }

                // CheckBox 상태 변경
                //CheckBox 상태 변경
                boolean isChecked = !voteChoiceList.get(pos).isChecked();
                voteChoiceList.get(pos).setChecked(isChecked);
                notifyDataSetChanged();

                if (isChecked) {
                    if (!voteSelected.contains(voteChoiceList.get(pos))) {
                        voteSelected.add(voteChoiceList.get(pos));
                    }
                } else {
                    voteSelected.remove(voteChoiceList.get(pos));
                }

                clickListener.onItemClick(pos);
            }

        }
    }
    /** 가장 많이 투표한 항목 계산 메서드 **/
    private int getMaxUserCount() {
        int maxCount = 0;
        for (BoardVoteInfoResponse.VoteChoice voteChoice : voteChoiceList) {
            int userCount = voteChoice.getVoteUserNickName().size();
            if (userCount > maxCount) {
                maxCount = userCount;
            }
        }
        return maxCount;
    }
}
