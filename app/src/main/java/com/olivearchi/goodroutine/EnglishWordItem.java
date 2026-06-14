package com.olivearchi.goodroutine;

import java.io.Serializable;

public class EnglishWordItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String word;
    private String meaning;
    private String example;
    private String phonetic;
    private int level; // 1: Basic, 2: Intermediate, 3: Advanced
    private boolean isFavorite;

    public EnglishWordItem(String word, String meaning, String example, String phonetic, int level) {
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.phonetic = phonetic;
        this.level = level;
        this.isFavorite = false;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getWord() { return word; }
    public String getMeaning() { return meaning; }
    public String getExample() { return example; }
    public String getPhonetic() { return phonetic; }
    public int getLevel() { return level; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
