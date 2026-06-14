package com.olivearchi.goodroutine;

import java.io.Serializable;

public class JapaneseWordItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String word;
    private String reading; // Furigana/Hiragana
    private String meaning;
    private String example;
    private int level; // 1: Basic, 2: Intermediate, 3: Advanced
    private boolean isFavorite;

    public JapaneseWordItem(String word, String reading, String meaning, String example, int level) {
        this.word = word;
        this.reading = reading;
        this.meaning = meaning;
        this.example = example;
        this.level = level;
        this.isFavorite = false;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getWord() { return word; }
    public String getReading() { return reading; }
    public String getMeaning() { return meaning; }
    public String getExample() { return example; }
    public int getLevel() { return level; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
