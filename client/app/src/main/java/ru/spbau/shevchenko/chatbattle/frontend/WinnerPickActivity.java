package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.BattleSearcher;
import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;

public class WinnerPickActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner_pick);
        ViewGroup playersLayout = (ViewGroup)findViewById(R.id.players_radio_group);

        Button cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(this);

        Button ok = (Button) findViewById(R.id.ok_button);
        ok.setOnClickListener(this);


        Intent intent = getIntent();
        ArrayList<Integer> playersID = (ArrayList<Integer>)intent.getSerializableExtra("playersId");
        for (int playerId : playersID) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(playerId);
            String x = Integer.toString(playerId);
            radioButton.setText(x);
            playersLayout.addView(radioButton);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_button: {
                finish();
                break;
            }
            case R.id.ok_button: {
                int chosen = ((RadioGroup)findViewById(R.id.players_radio_group)).getCheckedRadioButtonId();
                if (chosen == -1) {
                    Toast.makeText(WinnerPickActivity.this, "choose plz", Toast.LENGTH_LONG).show();
                    break;
                }
                RequestMaker.sendRequest(RequestMaker.domainName + "/chat/close/" + ProfileManager.getPlayer().id + "/" + chosen, RequestMaker.Method.POST, new RequestCallback() {
                    @Override
                    public void run(String response) {}
                });
                finish();
            }
        }
    }


}
