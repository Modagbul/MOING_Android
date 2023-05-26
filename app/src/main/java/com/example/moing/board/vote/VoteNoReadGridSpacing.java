package com.example.moing.board.vote;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class VoteNoReadGridSpacing extends RecyclerView.ItemDecoration {
    private int spanCount; // 그리드의 열 수
    private int spacing; // 아이템 간격

    public VoteNoReadGridSpacing(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // spanCount = 4, spacing = 8dp
        int position = parent.getChildAdapterPosition(view); // 아이템 위치
        int column = position % spanCount; // 아이템이 속한 열의 인덱스

        // 아이템 별 간격 설정
        // 첫번째 행에 있는 아이템인 경우 상단 여백 추가
        if (position < spanCount)
            outRect.top = spacing;

        outRect.left = spacing;
        outRect.bottom = spacing;
//        // 첫 번째 열 왼쪽 여백
//        if (column == 0) {
//            outRect.left = spacing * 4;
//        }
//        // 마지막 열 오른쪽 여백
//        if (column == spanCount -1) {
//            outRect.right = spacing * 5;
//        }



    }
}
