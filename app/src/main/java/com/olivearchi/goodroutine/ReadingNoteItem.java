package com.olivearchi.goodroutine;

import java.io.Serializable;

public class ReadingNoteItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String bookTitle;
    private String content;
    private String remarks;
    private String modifiedDateTime;
    private boolean isFavorite;

    public ReadingNoteItem(String bookTitle, String content, String remarks, String modifiedDateTime) {
        this.bookTitle = bookTitle;
        this.content = content;
        this.remarks = remarks;
        this.modifiedDateTime = modifiedDateTime;
        this.isFavorite = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getModifiedDateTime() {
        return modifiedDateTime;
    }

    public void setModifiedDateTime(String modifiedDateTime) {
        this.modifiedDateTime = modifiedDateTime;
    }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
