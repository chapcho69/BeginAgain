package com.olivearchi.goodroutine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JapaneseWordAdapter extends RecyclerView.Adapter<JapaneseWordAdapter.ViewHolder> {

    private List<JapaneseWordItem> words;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JapaneseWordItem item);
    }

    public JapaneseWordAdapter(List<JapaneseWordItem> words, OnItemClickListener listener) {
        this.words = words;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_japanese_word, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JapaneseWordItem item = words.get(position);
        holder.text1.setText(item.getWord() + " (" + item.getReading() + ")");
        holder.text2.setText(item.getMeaning());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.text_word);
            text2 = itemView.findViewById(R.id.text_meaning);
        }
    }
}
