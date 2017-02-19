package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import java.io.ByteArrayOutputStream;

import ru.spbau.shevchenko.chatbattle.R;

public class WhiteboardActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private DrawingView drawView;
    private RoundColoredButton currPaint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whiteboard);
        drawView = (DrawingView) findViewById(R.id.drawing);


        final LinearLayout paletteTopRow = (LinearLayout) findViewById(R.id.palette_top_row);
        final LinearLayout paletteBottomRow = (LinearLayout) findViewById(R.id.palette_bottom_row);
        int[] topRowColors = getResources().getIntArray(R.array.palette_top_row);
        int[] bottomRowColors = getResources().getIntArray(R.array.palette_bottom_row);
        fillPaletteRow(paletteTopRow, topRowColors);
        fillPaletteRow(paletteBottomRow, bottomRowColors);

        // Set initial color
        paintClicked(paletteTopRow.getChildAt(0));


        final SeekBar brushSizeBar = (SeekBar) findViewById(R.id.brush_size_seekbar);
        brushSizeBar.setOnSeekBarChangeListener(this);
        final View saveBtn = findViewById(R.id.send_btn);
        saveBtn.setOnClickListener(this);

        int maxBrush = brushSizeBar.getMax();
        float initBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxBrush / 2, getResources().getDisplayMetrics());

        brushSizeBar.setProgress(maxBrush / 2);
        drawView.setBrushSize(initBrushSize);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_btn: {
                onSendButtonClick();
                break;
            }
            default: { // paintClicked
                paintClicked(view);
            }
        }
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

    private void paintClicked(View view) {
        if (view != currPaint) {
            final RoundColoredButton clickedButton = (RoundColoredButton) view;
            final int color = (int) view.getTag();
            drawView.setColor(color);
            if (currPaint != null) {
                currPaint.setClicked(false);
            }
            clickedButton.setClicked(true);
            currPaint = clickedButton;

        }
    }

    private void fillPaletteRow(LinearLayout paletteTopRow, int[] rowColors) {
        @SuppressWarnings("NumericCastThatLosesPrecision")
        int paletteButtonSize = (int) getResources().getDimension(R.dimen.palette_button_size);
        for (int color : rowColors) {
            RoundColoredButton button = RoundColoredButton.create(this, color);
            button.setOnClickListener(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(paletteButtonSize, paletteButtonSize);
            @SuppressWarnings("NumericCastThatLosesPrecision")
            int margin = (int) getResources().getDimension(R.dimen.palette_button_margin);
            params.setMargins(margin, margin, margin, margin);
            button.setLayoutParams(params);
            paletteTopRow.addView(button);

        }
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
}
