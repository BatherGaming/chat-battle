package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button playButton = (Button)findViewById(R.id.playButton);
        playButton.setOnClickListener(this);
        Button profileButton = (Button)findViewById(R.id.profileButton);
        profileButton.setOnClickListener(this);

        // TODO: move it to ProfileActivity
        // TODO: handle different ids
        RequestMaker.sendRequest("http://qwsafex.pythonanywhere.com/players/1", "",
                RequestMaker.Method.GET,
                new RequestCallback() {
            @Override
            public void run(String response) {
                ProfileManager.onServerResponse(response);
            }
        });

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.playButton: {
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.profileButton: {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            }

        }
    }

}
