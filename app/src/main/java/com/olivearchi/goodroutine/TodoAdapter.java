package com.olivearchi.goodroutine;

import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<TodoItem> todoList;
    private OnItemClickListener listener;
    private OnDoneChangeListener doneListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnDoneChangeListener {
        void onDoneChanged(TodoItem item, boolean isChecked);
    }

    public TodoAdapter(List<TodoItem> todoList, OnItemClickListener listener, OnDoneChangeListener doneListener) {
        this.todoList = todoList;
        this.listener = listener;
        this.doneListener = doneListener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view, listener, doneListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem item = todoList.get(position);
        android.content.Context context = holder.itemView.getContext();
        
        // Removed Indentation effect: Set constant margin
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
        params.leftMargin = (int) (12 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
        holder.cardView.setLayoutParams(params);

        String subject = item.getSubject();
        if (subject != null && subject.length() > 5) {
            subject = subject.substring(0, 5) + "...";
        }
        
        String repeatStr = context.getString(R.string.repeat_none);
        if (item.isRepeating()) {
            switch (item.getRepeatType()) {
                case TodoItem.REPEAT_DAY: repeatStr = context.getString(R.string.repeat_daily); break;
                case TodoItem.REPEAT_WEEK: repeatStr = context.getString(R.string.repeat_weekly); break;
                case TodoItem.REPEAT_MONTH: repeatStr = context.getString(R.string.repeat_monthly); break;
                case TodoItem.REPEAT_YEAR: repeatStr = context.getString(R.string.repeat_yearly); break;
            }
        }

        String timeStr = "";
        try {
            String fullDateTime = item.getStartDateTime();
            if (fullDateTime != null && fullDateTime.contains(" ")) {
                timeStr = fullDateTime.split(" ")[1];
            }
        } catch (Exception e) {
            Log.e("TodoAdapter", "Error parsing time", e);
        }
        
        String displayText = subject + " / " + repeatStr;
        if (!timeStr.isEmpty()) {
            displayText += " / " + timeStr;
        }
        displayText += " / " + item.getPerformCount() + context.getString(R.string.status_count_unit);
        holder.taskText.setText(displayText);
        
        int backgroundColor = item.getColor();
        int textColor = ColorUtils.getContrastColor(backgroundColor);
        
        holder.cardView.setCardBackgroundColor(ColorStateList.valueOf(backgroundColor));
        holder.taskText.setTextColor(textColor);
        holder.statusIcon.setColorFilter(textColor);

        // Check if end date has passed (for strike-thru and angry emoticon)
        boolean isDelayed = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date endDate = sdf.parse(item.getEndDateTime());
            if (endDate != null && endDate.before(new Date())) {
                holder.taskText.setPaintFlags(holder.taskText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                if (!item.isDone()) {
                    isDelayed = true;
                }
            } else {
                holder.taskText.setPaintFlags(holder.taskText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        } catch (Exception e) {
            Log.e("TodoAdapter", "Error parsing date", e);
            holder.taskText.setPaintFlags(holder.taskText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        
        // Emoticon display logic
        if (item.isDone()) {
            holder.statusIcon.setImageResource(R.drawable.ic_check);
            holder.statusIcon.setVisibility(View.VISIBLE);
            holder.emoticonText.setVisibility(View.GONE);
        } else {
            holder.statusIcon.setVisibility(View.GONE);
            holder.emoticonText.setVisibility(View.VISIBLE);
            if (isDelayed) {
                holder.emoticonText.setText("😡");
            } else {
                holder.emoticonText.setText(item.getEmoticon());
            }
            holder.emoticonText.setTextColor(textColor);
        }
        
        holder.checkBox.setButtonTintList(ColorStateList.valueOf(textColor));

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isDone());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (doneListener != null) {
                doneListener.onDoneChanged(item, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView taskText;
        TextView emoticonText;
        CheckBox checkBox;
        ImageView statusIcon;
        MaterialCardView cardView;

        public TodoViewHolder(@NonNull View itemView, OnItemClickListener listener, OnDoneChangeListener doneListener) {
            super(itemView);
            taskText = itemView.findViewById(R.id.todo_text);
            emoticonText = itemView.findViewById(R.id.emoticon_text);
            checkBox = itemView.findViewById(R.id.todo_checkbox);
            statusIcon = itemView.findViewById(R.id.status_icon);
            cardView = itemView.findViewById(R.id.todo_card);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                }
            });
        }
    }
}
