package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import java.io.ByteArrayOutputStream;

import ru.spbau.shevchenko.chatbattle.R;

public class WhiteboardActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private DrawingView drawView;
    private ImageButton currPaint;
    private float smallBrush, mediumBrush, largeBrush;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whiteboard);
        drawView = (DrawingView) findViewById(R.id.drawing);

        // Set initial color
        final LinearLayout paintLayout1 = (LinearLayout) findViewById(R.id.paint_colors1);
        paintClicked(paintLayout1.getChildAt(0));

        final LinearLayout paintLayout2 = (LinearLayout) findViewById(R.id.paint_colors2);
        Resources resources = getResources();
        for (int i = 0; i < paintLayout1.getChildCount(); i++) {
            paintLayout1.getChildAt(i).setOnClickListener(this);
        }
        for (int i = 0; i < paintLayout2.getChildCount(); i++) {
            paintLayout2.getChildAt(i).setOnClickListener(this);
        }


        final SeekBar brushSizeBar = (SeekBar) findViewById(R.id.brush_size_seekbar);
        brushSizeBar.setOnSeekBarChangeListener(this);
        final ImageButton drawBtn = (ImageButton) findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);
        final ImageButton saveBtn = (ImageButton) findViewById(R.id.send_btn);
        saveBtn.setOnClickListener(this);
        final ImageButton eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        int maxBrush = brushSizeBar.getMax();
        smallBrush = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxBrush / 4, getResources().getDisplayMetrics());
        mediumBrush = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxBrush / 2, getResources().getDisplayMetrics());
        largeBrush = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3 * maxBrush / 4, getResources().getDisplayMetrics());

        brushSizeBar.setProgress(maxBrush / 2);
        drawView.setBrushSize(mediumBrush);
    }

    public void paintClicked(View view) {
        if (drawView.isErase()){
            drawView.setErase(false);
            drawView.setBrushSize(drawView.getLastBrushSize());
        }
        if (view != currPaint) {
            final ImageButton imgView = (ImageButton) view;
            final String color = view.getTag().toString();
            drawView.setColor(color);
            imgView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.whiteboard_paint_pressed, null));
            if (currPaint != null) {
                currPaint.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.whiteboard_paint, null));
            }
            currPaint = (ImageButton) view;

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.draw_btn: {
                onDrawButtonClick();
                break;
            }
            case R.id.send_btn: {
                onSendButtonClick();
                break;
            }
            case R.id.erase_btn: {
                onEraseButtonClick();
                break;
            }
            default: { // paintClicked
                paintClicked(view);
            }
        }
    }

    private void onDrawButtonClick() {
        final Dialog brushDialog = new Dialog(this);
        brushDialog.setTitle(getResources().getString(R.string.brush_size));
        brushDialog.setContentView(R.layout.brush_chooser);

        setupButtons(false, brushDialog);

        brushDialog.show();
    }

    private void onSendButtonClick() {
        final ByteArrayOutputStream pngOutStream = new ByteArrayOutputStream();
        final Bitmap bitmap = drawView.getCanvasBitmap();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngOutStream);

        final Intent intent = new Intent();
        intent.putExtra("whiteboard", pngOutStream.toByteArray());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onEraseButtonClick() {
        final Dialog brushDialog = new Dialog(this);
        brushDialog.setTitle(getResources().getString(R.string.eraser_size));
        brushDialog.setContentView(R.layout.brush_chooser);

        setupButtons(true, brushDialog);

        brushDialog.show();
    }

    private void setupButtons(final boolean eraserMode, final Dialog brushDialog) {
        final ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
        final ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
        final ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);

        setupButton(smallBtn, eraserMode, smallBrush, brushDialog);
        setupButton(mediumBtn, eraserMode, mediumBrush, brushDialog);
        setupButton(largeBtn, eraserMode, largeBrush, brushDialog);
    }

    private void setupButton(ImageButton imageButton, final boolean eraserMode,
                             final float brushSize, final DialogInterface brushDialog) {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(eraserMode);
                drawView.setBrushSize(brushSize);
                brushDialog.dismiss();
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
        float newSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
        drawView.setBrushSize(newSize);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
