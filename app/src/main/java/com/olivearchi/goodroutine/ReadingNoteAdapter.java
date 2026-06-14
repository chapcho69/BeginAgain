package com.olivearchi.goodroutine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReadingNoteAdapter extends RecyclerView.Adapter<ReadingNoteAdapter.ViewHolder> {

    private List<ReadingNoteItem> notes;
    private OnItemClickListener listener;
    private OnPlayClickListener playListener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnItemClickListener {
        void onItemClick(ReadingNoteItem item);
    }

    public interface OnPlayClickListener {
        void onPlayClick(String text);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(ReadingNoteItem item);
    }

    public ReadingNoteAdapter(List<ReadingNoteItem> notes, OnItemClickListener listener, OnPlayClickListener playListener, OnFavoriteClickListener favoriteListener) {
        this.notes = notes;
        this.listener = listener;
        this.playListener = playListener;
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reading_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReadingNoteItem item = notes.get(position);
        holder.dateText.setText(item.getModifiedDateTime());
        holder.contentText.setText(item.getContent());
        holder.bookText.setText("책제목: " + item.getBookTitle());
        
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
        return notes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, contentText, bookText;
        ImageButton btnPlay, btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.text_note_date);
            contentText = itemView.findViewById(R.id.text_note_content);
            bookText = itemView.findViewById(R.id.text_note_book);
            btnPlay = itemView.findViewById(R.id.btn_play_tts);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}
