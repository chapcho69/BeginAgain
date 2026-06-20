package com.olivearchi.goodroutine;

public class SearchResultItem {
    public enum Type { HABIT, READING, MEMORIZATION, MEMO, TASK, JAPANESE }

    private Type type;
    private Object item;
    private String title;
    private String content;

    public SearchResultItem(Type type, Object item, String title, String content) {
        this.type = type;
        this.item = item;
        this.title = title;
        this.content = content;
    }

    public Type getType() { return type; }
    public Object getItem() { return item; }
    public String getTitle() { return title; }
    public String getContent() { return content; }

    public int getTypeResId() {
        switch (type) {
            case HABIT: return R.string.feature_routine;
            case READING: return R.string.feature_reading;
            case MEMORIZATION: return R.string.feature_memorization;
            case MEMO: return R.string.feature_memo;
            case TASK: return R.string.feature_today;
            case JAPANESE: return R.string.feature_japanese;
            default: return 0;
        }
    }
}
