package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int DRAW_WHITEBOARD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Button whiteboardBtn = (Button) findViewById(R.id.whiteboard);
        whiteboardBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.whiteboard) {
            Intent intent = new Intent(this, WhiteboardActivity.class);
            startActivityForResult(intent, DRAW_WHITEBOARD);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_WHITEBOARD) {
            if (resultCode != RESULT_OK) {
                return;
            }
            final byte[] whiteboardBytes = data.getByteArrayExtra("whiteboard");

            final Toast completedToast = Toast.makeText(this, "Upload completed", Toast.LENGTH_LONG);
            JSONObject whiteboardJSON = null;
            try {
                whiteboardJSON = (new JSONObject()).put("whiteboard", Base64.encodeToString(whiteboardBytes, Base64.NO_WRAP));
            } catch (JSONException e) {
                Log.e("onActResult", e.getMessage());
            }
            RequestMaker.sendWhiteboard(whiteboardJSON.toString(), new RequestCallback() {
                @Override
                public void run(String response) {
                    completedToast.setText(response);
                    completedToast.show();
                }
            });
        }
    }
}
