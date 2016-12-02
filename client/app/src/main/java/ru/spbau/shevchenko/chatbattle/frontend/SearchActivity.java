package ru.spbau.shevchenko.chatbattle.frontend;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.BattleSearcher;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;

public class SearchActivity extends BasicActivity implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final Button search_as_player_button = (Button) findViewById(R.id.search_as_player_button);
        search_as_player_button.setOnClickListener(this);

        final Button search_as_leader_button = (Button) findViewById(R.id.search_as_leader_button);
        search_as_leader_button.setOnClickListener(this);

        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar);
        switch (ProfileManager.getPlayerStatus()) {
            case IDLE: {
                spinner.setVisibility(View.GONE);
                break;
            }
            case IN_QUEUE_AS_PLAYER:
            case IN_QUEUE_AS_LEADER: {
                spinner.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_as_player_button: {
                ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IN_QUEUE_AS_PLAYER);
                BattleSearcher.findBattle(Player.Role.PLAYER);
                final ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar);
                spinner.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.search_as_leader_button: {
                ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IN_QUEUE_AS_LEADER);
                final ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar);
                spinner.setVisibility(View.VISIBLE);
                BattleSearcher.findBattle(Player.Role.LEADER);
                break;
            }
        }
    }
}
