package ru.spbau.shevchenko.chatbattle.frontend;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Locale;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;

public class ProfileActivity extends BasicActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Display profile info
        Player player = ProfileManager.getPlayer();
        TextView loginView = (TextView) findViewById(R.id.login_value_view);
        loginView.setText(player.getLogin());
        TextView ageView = (TextView) findViewById(R.id.age_value_view);
        ageView.setText(String.format(Locale.getDefault(), "%d", player.getAge()));
        TextView sexView = (TextView) findViewById(R.id.sex_value_view);
        sexView.setText(player.getSex().toString());
        TextView ratingView = (TextView) findViewById(R.id.rating_value_view);
        ratingView.setText(String.format(Locale.getDefault(), "%d", player.getRating()));
    }
}
