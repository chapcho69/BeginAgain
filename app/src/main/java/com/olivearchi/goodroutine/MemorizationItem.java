package com.olivearchi.goodroutine;

import java.io.Serializable;

public class MemorizationItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String title;
    private String content;
    private String keyword;
    private String createdAt;
    private String updatedAt;
    private boolean isFavorite;

    public MemorizationItem(String title, String content, String keyword, String createdAt, String updatedAt) {
        this.title = title;
        this.content = content;
        this.keyword = keyword;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isFavorite = false;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
