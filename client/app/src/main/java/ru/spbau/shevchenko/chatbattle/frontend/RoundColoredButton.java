package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Button;


public class RoundColoredButton extends Button {
    private static final float CLICKED_BORDER_PERCENT = 0.2f;
    private static final float CLICKED_SIZE_PERCENT = 0.85f;
    private final Paint fillPaint;
    private final Paint borderPaint;
    private int w;
    private int h;
    private boolean clicked = false;

    public static RoundColoredButton create(Context context, int color){
        RoundColoredButton button = new RoundColoredButton(context);
        button.fillPaint.setColor(color);
        button.setTag(color);
        return button;
    }

    public RoundColoredButton(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        clicked = false;
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;
        borderPaint.setStrokeWidth(w / 2 * CLICKED_SIZE_PERCENT * CLICKED_BORDER_PERCENT);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int radius = Math.min(w, h) / 2;
        if (clicked) {
            float borderWidth = borderPaint.getStrokeWidth();
            float clickedRadius = radius * CLICKED_SIZE_PERCENT;
            // Subtract half to insure absence of gap
            canvas.drawCircle(w/2, h/2, clickedRadius - borderWidth / 2, fillPaint);
            // Subtract half border to make it's total size exactly that of view
            canvas.drawCircle(w/2, h/2, clickedRadius - borderWidth / 2, borderPaint);
        }
        else {
            canvas.drawCircle(w/2, h/2, radius, fillPaint);
        }
    }
    public void setClicked(boolean whether) {
        if (clicked == whether) return;
        clicked = whether;
        invalidate();
    }
}
