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

    public String getTypeName() {
        switch (type) {
            case HABIT: return "습관";
            case READING: return "독서노트";
            case MEMORIZATION: return "암기장";
            case MEMO: return "메모";
            case TASK: return "할일들";
            case JAPANESE: return "일본어";
            default: return "";
        }
    }
}
