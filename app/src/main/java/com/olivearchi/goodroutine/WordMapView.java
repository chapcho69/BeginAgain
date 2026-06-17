package com.olivearchi.goodroutine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordMapView extends View {
    private final List<WordPos> wordsToDraw = new ArrayList<>();
    private final Random random = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float scaleFactor = 1.0f;
    private float translateX = 0f;
    private float translateY = 0f;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private static class WordPos {
        String text;
        float x, y;
        int size;
        int color;

        WordPos(String text, float x, float y, int size, int color) {
            this.text = text; this.x = x; this.y = y; this.size = size; this.color = color;
        }
    }

    public WordMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 5.0f));
                invalidate();
                return true;
            }
        });

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                translateX -= distanceX;
                translateY -= distanceY;
                invalidate();
                return true;
            }
        });
    }

    public void setWords(Map<String, Integer> wordFrequencies) {
        wordsToDraw.clear();
        translateX = 0;
        translateY = 0;
        scaleFactor = 1.0f;
        
        if (wordFrequencies == null || wordFrequencies.isEmpty()) {
            invalidate();
            return;
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(wordFrequencies.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        int count = Math.min(list.size(), 50);

        int maxFreq = list.get(0).getValue();
        int minFreq = list.get(count - 1).getValue();

        post(() -> {
            int width = getWidth();
            int height = getHeight();
            if (width == 0 || height == 0) return;

            for (int i = 0; i < count; i++) {
                Map.Entry<String, Integer> entry = list.get(i);
                float ratio = (maxFreq == minFreq) ? 0.5f : (float) (entry.getValue() - minFreq) / (maxFreq - minFreq);
                int size = (int) (20 * getResources().getDisplayMetrics().scaledDensity + (40 * ratio * getResources().getDisplayMetrics().scaledDensity));

                // Distribution in a larger area to allow panning
                float x = (random.nextFloat() * width * 2) - (width * 0.5f);
                float y = (random.nextFloat() * height * 2) - (height * 0.5f);
                
                int[] colors = {0xFF1976D2, 0xFF388E3C, 0xFFF57C00, 0xFF7B1FA2, 0xFF455A64};
                int color = colors[random.nextInt(colors.length)];

                wordsToDraw.add(new WordPos(entry.getKey(), x, y, size, color));
            }
            invalidate();
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = scaleGestureDetector.onTouchEvent(event);
        handled = gestureDetector.onTouchEvent(event) || handled;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
        }
        return handled || super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor, getWidth() / 2f - translateX, getHeight() / 2f - translateY);

        for (WordPos wp : wordsToDraw) {
            paint.setTextSize(wp.size);
            paint.setColor(wp.color);
            paint.setAlpha(200);
            canvas.drawText(wp.text, wp.x, wp.y, paint);
        }
        canvas.restore();
    }
}
