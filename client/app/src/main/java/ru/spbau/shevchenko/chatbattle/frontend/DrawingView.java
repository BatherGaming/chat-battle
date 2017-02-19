package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import ru.spbau.shevchenko.chatbattle.R;


public class DrawingView extends View {
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = ContextCompat.getColor(getContext(), R.color.black);
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float brushSize;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    public void setBrushSize(float newSize) {
        brushSize = newSize;
        drawPaint.setStrokeWidth(brushSize);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float touchX = event.getX();
        final float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setColor(int newColor) {
        invalidate();
        paintColor = newColor;
        drawPaint.setColor(paintColor);
    }

    public Bitmap getCanvasBitmap() {
        return canvasBitmap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

        int minX = getPaddingLeft();
        int maxX = getWidth() - getPaddingRight();
        int minY = getPaddingTop();
        int maxY = getHeight() - getPaddingTop();
        drawCanvas.clipRect(minX, minY, maxX, maxY, Region.Op.REPLACE);

        // Fill canvas with white
        Paint fillWhite = new Paint();
        fillWhite.setColor(Color.WHITE);
        fillWhite.setStyle(Paint.Style.FILL);
        drawCanvas.drawPaint(fillWhite);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    private void setupDrawing() {
        //get drawing area setup for interaction
        brushSize = 1;

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);


    }
}
