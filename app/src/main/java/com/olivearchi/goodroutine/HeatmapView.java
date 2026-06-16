package com.olivearchi.goodroutine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HeatmapView extends View {
    private java.util.Map<String, Integer> data;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float boxSize;
    private float boxMargin;
    private int rows = 7;
    private int columns = 53;

    public HeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        boxSize = 14 * density;
        boxMargin = 4 * density;
    }

    public void setData(java.util.Map<String, Integer> data) {
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null) return;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        // Align to the start of the week (Sunday)
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int c = 0; columns > c; c++) {
            for (int r = 0; rows > r; r++) {
                String dateKey = sdf.format(cal.getTime());
                int count = data.containsKey(dateKey) ? data.get(dateKey) : 0;
                
                paint.setColor(getColorForCount(count));
                
                float left = c * (boxSize + boxMargin);
                float top = r * (boxSize + boxMargin);
                canvas.drawRoundRect(new RectF(left, top, left + boxSize, top + boxSize), 4f, 4f, paint);
                
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
    }

    private int getColorForCount(int count) {
        if (count == 0) return 0xFFEBEDF0;
        if (count <= 2) return 0xFFC6E48B;
        if (count <= 5) return 0xFF7BC96F;
        if (count <= 10) return 0xFF239A3B;
        return 0xFF196127;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = (int) (columns * (boxSize + boxMargin));
        int h = (int) (rows * (boxSize + boxMargin));
        setMeasuredDimension(w, h);
    }
}
