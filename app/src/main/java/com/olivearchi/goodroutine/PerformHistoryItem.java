package com.olivearchi.goodroutine;

import java.io.Serializable;

public class PerformHistoryItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private long todoId;
    private String category;
    private String performDateTime;

    public PerformHistoryItem(long todoId, String category, String performDateTime) {
        this.todoId = todoId;
        this.category = category;
        this.performDateTime = performDateTime;
    }

    public long getTodoId() {
        return todoId;
    }

    public String getCategory() {
        return category;
    }

    public String getPerformDateTime() {
        return performDateTime;
    }
}
