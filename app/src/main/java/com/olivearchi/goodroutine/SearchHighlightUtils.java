package com.olivearchi.goodroutine;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

public class SearchHighlightUtils {
    private static String currentSearchQuery = "";

    public static void setSearchQuery(String query) {
        currentSearchQuery = (query != null) ? query.toLowerCase() : "";
    }

    public static String getSearchQuery() {
        return currentSearchQuery;
    }

    public static void clearQuery() {
        currentSearchQuery = "";
    }

    public static CharSequence getHighlightedText(String fullText) {
        if (fullText == null || currentSearchQuery.isEmpty()) return fullText;
        
        SpannableString spannable = new SpannableString(fullText);
        String lowerFullText = fullText.toLowerCase();
        
        int start = 0;
        while ((start = lowerFullText.indexOf(currentSearchQuery, start)) != -1) {
            int end = start + currentSearchQuery.length();
            spannable.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end;
        }
        
        return spannable;
    }
}
