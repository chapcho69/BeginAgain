package com.olivearchi.goodroutine;

import java.util.List;

public class WordDataHolder {
    private static List<EnglishWordItem> englishWords;
    private static List<JapaneseWordItem> japaneseWords;

    public static void setEnglishWords(List<EnglishWordItem> words) { englishWords = words; }
    public static List<EnglishWordItem> getEnglishWords() { return englishWords; }

    public static void setJapaneseWords(List<JapaneseWordItem> words) { japaneseWords = words; }
    public static List<JapaneseWordItem> getJapaneseWords() { return japaneseWords; }
}
