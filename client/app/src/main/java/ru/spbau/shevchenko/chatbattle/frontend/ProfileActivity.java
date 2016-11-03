package ru.spbau.shevchenko.chatbattle.frontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Display profile info
        Player player = ProfileManager.getPlayerInfo();
        TextView login_view = (TextView) findViewById(R.id.login_value_view);
        login_view.setText(player.login);
        TextView age_view = (TextView) findViewById(R.id.age_value_view);
        age_view.setText(Integer.toString(player.age));
        TextView sex_view = (TextView) findViewById(R.id.sex_value_view);
        sex_view.setText(player.sex.toString());
    }
}
