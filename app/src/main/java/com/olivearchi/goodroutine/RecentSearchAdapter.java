package com.olivearchi.goodroutine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {

    private List<String> history;
    private OnRecentClickListener listener;

    public interface OnRecentClickListener {
        void onRecentClick(String query);
        void onDeleteClick(String query);
    }

    public RecentSearchAdapter(List<String> history, OnRecentClickListener listener) {
        this.history = history;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = history.get(position);
        holder.textQuery.setText(query);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecentClick(query);
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(query);
        });
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textQuery;
        ImageView btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textQuery = itemView.findViewById(R.id.text_recent_query);
            btnDelete = itemView.findViewById(R.id.btn_delete_recent);
        }
    }
}
