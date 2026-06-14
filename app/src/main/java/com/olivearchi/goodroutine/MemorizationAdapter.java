package com.olivearchi.goodroutine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemorizationAdapter extends RecyclerView.Adapter<MemorizationAdapter.ViewHolder> {

    private List<MemorizationItem> items;
    private OnItemClickListener listener;
    private OnPlayClickListener playListener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnItemClickListener {
        void onItemClick(MemorizationItem item);
    }

    public interface OnPlayClickListener {
        void onPlayClick(String text);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(MemorizationItem item);
    }

    public MemorizationAdapter(List<MemorizationItem> items, OnItemClickListener listener, OnPlayClickListener playListener, OnFavoriteClickListener favoriteListener) {
        this.items = items;
        this.listener = listener;
        this.playListener = playListener;
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memorization, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemorizationItem item = items.get(position);
        holder.titleText.setText(item.getTitle());
        holder.contentText.setText(item.getContent());
        holder.keywordText.setText(item.getKeyword());
        holder.dateText.setText("수정: " + item.getUpdatedAt());

        holder.btnFavorite.setImageResource(item.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_border);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.btnPlay.setOnClickListener(v -> {
            if (playListener != null) playListener.onPlayClick(item.getContent());
        });

        holder.btnFavorite.setOnClickListener(v -> {
            boolean newState = !item.isFavorite();
            item.setFavorite(newState);
            holder.btnFavorite.setImageResource(newState ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
            if (favoriteListener != null) favoriteListener.onFavoriteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, contentText, keywordText, dateText;
        ImageButton btnPlay, btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_memo_title);
            contentText = itemView.findViewById(R.id.text_memo_content);
            keywordText = itemView.findViewById(R.id.text_memo_keyword);
            dateText = itemView.findViewById(R.id.text_memo_date);
            btnPlay = itemView.findViewById(R.id.btn_memo_play);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}
