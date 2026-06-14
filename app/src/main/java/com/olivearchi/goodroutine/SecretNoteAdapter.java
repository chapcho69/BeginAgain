package com.olivearchi.goodroutine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SecretNoteAdapter extends RecyclerView.Adapter<SecretNoteAdapter.ViewHolder> {
    private List<SecretNoteItem> notes;
    private OnItemClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnItemClickListener { void onItemClick(SecretNoteItem item); }
    public interface OnFavoriteClickListener { void onFavoriteClick(SecretNoteItem item, boolean newState); }

    public SecretNoteAdapter(List<SecretNoteItem> notes, OnItemClickListener listener, OnFavoriteClickListener favoriteListener) {
        this.notes = notes;
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_secret_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SecretNoteItem item = notes.get(position);
        holder.titleText.setText(item.getTitle());
        holder.contentText.setText(item.getContent());
        holder.dateText.setText(item.getCreatedAt());
        holder.btnFavorite.setImageResource(item.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_border);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnFavorite.setOnClickListener(v -> {
            boolean newState = !item.isFavorite();
            item.setFavorite(newState);
            holder.btnFavorite.setImageResource(newState ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
            favoriteListener.onFavoriteClick(item, newState);
        });
    }

    @Override
    public int getItemCount() { return notes.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, contentText, dateText;
        ImageButton btnFavorite;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_secret_title);
            contentText = itemView.findViewById(R.id.text_secret_content_preview);
            dateText = itemView.findViewById(R.id.text_secret_date);
            btnFavorite = itemView.findViewById(R.id.btn_secret_favorite);
        }
    }
}
