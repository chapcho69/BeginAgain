package com.olivearchi.goodroutine;

import android.graphics.Color;

public class ColorUtils {
    /**
     * Returns either black or white depending on the background color's luminance.
     * Uses the W3C recommended formula for relative luminance.
     */
    public static int getContrastColor(int backgroundColor) {
        // Extract RGB components
        int red = Color.red(backgroundColor);
        int green = Color.green(backgroundColor);
        int blue = Color.blue(backgroundColor);
        
        // Calculate perceived brightness (luminance)
        // Formula: (0.299*R + 0.587*G + 0.114*B)
        double darkness = 1 - (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
        
        if (darkness < 0.5) {
            return Color.BLACK; // Light background, use black text
        } else {
            return Color.WHITE; // Dark background, use white text
        }
    }
}
