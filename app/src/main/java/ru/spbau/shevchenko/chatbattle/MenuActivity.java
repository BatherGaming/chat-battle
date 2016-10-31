package ru.spbau.shevchenko.chatbattle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button playButton = (Button)findViewById(R.id.playButton);
        playButton.setOnClickListener(this);
        Button profileButton = (Button)findViewById(R.id.profileButton);
        profileButton.setOnClickListener(this);
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
