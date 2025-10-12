package com.SIMATS.smartcity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class ArcGaugeView extends View {

    private Paint bgPaint, progressPaint, textPaint;
    private float progress = 0f; // 0–100
    private int strokeWidth = 40;
    private int bgColor = 0xFFD1D1D6;     // Light gray background
    private int progressColor = 0xFF30D158; // Green progress
    private int textColor = 0xFF000000;
    private float textSize = 64f;

    public ArcGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcGaugeView(Context context) {
        super(context);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(strokeWidth);
        bgPaint.setColor(bgColor);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();

        float padding = strokeWidth / 2f + 10;
        float left = padding, top = padding;
        float right = w - padding, bottom = 2*(h - padding); // draws semicircle

        // Draw background arc (180° from 180 to 360)
        canvas.drawArc(left, top, right, bottom, 180, 180, false, bgPaint);

        // Draw progress arc
        float sweep = (progress / 100f) * 180f;
        canvas.drawArc(left, top, right, bottom, 180, sweep, false, progressPaint);

        // Draw percentage text
        String txt = String.format("%.0f%%", progress);
        canvas.drawText(txt, w / 2f, h - padding - textSize / 2f, textPaint);
    }

    public void setProgress(float value) {
        this.progress = Math.max(0f, Math.min(100f, value));
        invalidate();
    }
}
