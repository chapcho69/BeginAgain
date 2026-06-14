package com.olivearchi.goodroutine;

import java.io.Serializable;

public class TodayTaskItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String title;
    private String description;
    private int estimatedMinutes;

    public TodayTaskItem(String title, String description, int estimatedMinutes) {
        this.title = title;
        this.description = description;
        this.estimatedMinutes = estimatedMinutes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }
}
