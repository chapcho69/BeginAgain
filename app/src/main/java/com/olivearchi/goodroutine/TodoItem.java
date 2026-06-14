package com.olivearchi.goodroutine;

import java.io.Serializable;

public class TodoItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_DAY = 1;
    public static final int REPEAT_WEEK = 2;
    public static final int REPEAT_MONTH = 3;
    public static final int REPEAT_YEAR = 4;

    private long id;
    private String subject;
    private String detail;
    private String startDateTime;
    private String endDateTime;
    private boolean isRepeating;
    private int repeatType; // 0: None, 1: Day, 2: Week, 3: Month, 4: Year
    private boolean isDone;
    private int color;
    private int performCount = 0;
    private double version;
    private String emoticon;

    public TodoItem(String subject, String detail, String startDateTime, String endDateTime, boolean isRepeating, int repeatType) {
        this.id = System.currentTimeMillis();
        this.subject = subject;
        this.detail = detail;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.isRepeating = isRepeating;
        this.repeatType = repeatType;
        this.isDone = false;
        this.color = 0xFFFFFFFF; // Default white
        this.version = 1.0; 
        this.emoticon = "😊";
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public int getPerformCount() {
        return performCount;
    }

    public void setPerformCount(int performCount) {
        this.performCount = performCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getEmoticon() {
        return emoticon;
    }

    public void setEmoticon(String emoticon) {
        this.emoticon = emoticon;
    }
}
