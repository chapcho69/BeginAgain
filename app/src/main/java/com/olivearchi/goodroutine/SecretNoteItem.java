package com.olivearchi.goodroutine;

import java.io.Serializable;

public class SecretNoteItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String title;
    private String content;
    private String remarks;
    private String createdAt;
    private boolean isFavorite;

    public SecretNoteItem(String title, String content, String remarks, String createdAt) {
        this.title = title;
        this.content = content;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
