package ru.spbau.shevchenko.chatbattle.frontend;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.BattleSearcher;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;

public class SearchActivity extends BasicActivity implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final Button search_as_player_button = (Button) findViewById(R.id.search_as_player_button);
        search_as_player_button.setOnClickListener(this);

        final Button search_as_leader_button = (Button) findViewById(R.id.search_as_leader_button);
        search_as_leader_button.setOnClickListener(this);

        final Button stop_searching_button = (Button) findViewById(R.id.stop_searching_button);
        stop_searching_button.setOnClickListener(this);

        Log.d("Player status:", ProfileManager.getPlayerStatus().toString());
        switch (ProfileManager.getPlayerStatus()) {
            case IDLE: {
                stop_searching();
                break;
            }
            case IN_QUEUE_AS_PLAYER: {
                search_as(Player.Role.PLAYER);
                break;
            }
            case IN_QUEUE_AS_LEADER: {
                search_as(Player.Role.LEADER);
                break;
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_as_player_button: {
                search_as(Player.Role.PLAYER);
                break;
            }
            case R.id.search_as_leader_button: {
                search_as(Player.Role.LEADER);
                break;
            }
            case R.id.stop_searching_button: {
                stop_searching();
                break;
            }
        }
    }

    private void stop_searching() {
        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);
        RequestMaker.deleteFromQueue(ProfileManager.getPlayer().getId());

        final Button stop_searching = (Button) findViewById(R.id.stop_searching_button);
        final TextView textView = (TextView) findViewById(R.id.queueing_status_text);
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        stop_searching.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
    }

    private void search_as(Player.Role role) {
        switch (role) {
            case PLAYER: {
                ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IN_QUEUE_AS_PLAYER);
                BattleSearcher.findBattle(Player.Role.PLAYER);
                break;
            }
            case LEADER: {
                ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IN_QUEUE_AS_LEADER);
                BattleSearcher.findBattle(Player.Role.LEADER);
                break;
            }
        }

        final Button stop_searching = (Button) findViewById(R.id.stop_searching_button);
        final TextView textView = (TextView) findViewById(R.id.queueing_status_text);
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar);
        String text = role == Player.Role.PLAYER ? getString(R.string.search_as_player)
                                                 : getString(R.string.search_as_leader);
        textView.setText(text);
        textView.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        stop_searching.setVisibility(View.VISIBLE);
    }

    public void searchAgain(boolean declined, Player.Role role) {
        if (declined) {
            stop_searching();
        } else {
            search_as(role);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Search", "onResume");
        switch(ProfileManager.getPlayerStatus()) {
            case IDLE: {
                stop_searching();
                break;
            }
            case IN_QUEUE_AS_LEADER: {
                search_as(Player.Role.LEADER);
                break;
            }
            case IN_QUEUE_AS_PLAYER: {
                search_as(Player.Role.PLAYER);
                break;
            }
            case CHATTING_AS_LEADER: {
                finish();
                break;
            }
            case CHATTING_AS_PLAYER: {
                finish();
                break;
            }
        }
    }
}
