package com.olivearchi.goodroutine;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<SearchResultItem> results;
    private OnItemClickListener listener;
    private String query = "";

    public interface OnItemClickListener {
        void onItemClick(SearchResultItem result);
    }

    public SearchAdapter(List<SearchResultItem> results, OnItemClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    public void setQuery(String query) {
        this.query = query != null ? query.toLowerCase() : "";
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultItem item = results.get(position);
        
        String typeName = holder.itemView.getContext().getString(item.getTypeResId());
        String typeTitle = "[" + typeName + "] " + item.getTitle();
        holder.textTitle.setText(getHighlightedText(typeTitle, query));
        
        holder.textContent.setText(getHighlightedText(item.getContent(), query));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    private CharSequence getHighlightedText(String fullText, String query) {
        if (fullText == null || query.isEmpty()) return fullText;
        
        SpannableString spannable = new SpannableString(fullText);
        String lowerFullText = fullText.toLowerCase();
        
        int start = 0;
        while ((start = lowerFullText.indexOf(query, start)) != -1) {
            int end = start + query.length();
            spannable.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end;
        }
        
        return spannable;
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textContent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_search_type_title);
            textContent = itemView.findViewById(R.id.text_search_content);
        }
    }
}
