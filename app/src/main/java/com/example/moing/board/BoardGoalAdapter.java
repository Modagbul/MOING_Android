package com.example.moing.board;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moing.R;
import com.example.moing.response.BoardNoReadNoticeResponse;
import com.example.moing.response.BoardNoReadVoteResponse;

import java.util.List;

public class BoardGoalAdapter<T> extends RecyclerView.Adapter<BoardGoalAdapter.BoardViewHolder> {
    private BoardGoalFragment context;
    private List<T> dataList;

    // 아이템을 3개만 보여준다.
    private int displayCount = 3;


    TextView boardTitle, boardContent;
    View boardLine;

    // 클릭 아이템 리스너
    private OnItemClickListener onItemClickListener = null;
    // 클릭 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // 공지 생성자
    public BoardGoalAdapter(List<T> dataList, BoardGoalFragment context) {
        this.dataList = dataList;
        this.context = context;
    }

    class BoardViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public BoardViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            boardTitle = mView.findViewById(R.id.tv_board_title);
            boardContent = mView.findViewById(R.id.tv_board_content);
            boardLine = mView.findViewById(R.id.view_board_line);

            // 아이템 클릭 이벤트 추가
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체 메서드 호출
                        if(onItemClickListener != null) {
                            onItemClickListener.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }


    @NonNull
    @Override
    public BoardGoalAdapter.BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_item_board, parent, false);
        return new BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardGoalAdapter.BoardViewHolder holder, int position) {
        // 값을 가져오는 곳
        T item = dataList.get(position);

        if (item instanceof BoardNoReadNoticeResponse.NoticeData) {
            if (position <= 2)
            {
                BoardNoReadNoticeResponse.NoticeData data = (BoardNoReadNoticeResponse.NoticeData) item;
                boardTitle.setText(data.getTitle());
                boardContent.setText(data.getContent());
                if (position == 2)
                    boardLine.setVisibility(View.GONE);
            }
        }
        else if (item instanceof BoardNoReadVoteResponse.VoteData) {
            if (position <= 2) {
                BoardNoReadVoteResponse.VoteData data = (BoardNoReadVoteResponse.VoteData) item;
                boardTitle.setText(data.getTitle());
                boardContent.setText(data.getContent());
                if (position == 2)
                    boardLine.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(dataList.size(), displayCount);
    }
}
