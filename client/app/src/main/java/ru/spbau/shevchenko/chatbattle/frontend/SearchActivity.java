package ru.spbau.shevchenko.chatbattle.frontend;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;

public class SearchActivity extends BasicActivity implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final Button searchAsPlayerButton = (Button) findViewById(R.id.search_as_player_button);
        searchAsPlayerButton.setOnClickListener(this);

        final Button searchAsLeaderButton = (Button) findViewById(R.id.search_as_leader_button);
        searchAsLeaderButton.setOnClickListener(this);

        final Button stopSearchingButton = (Button) findViewById(R.id.stop_searching_button);
        stopSearchingButton.setOnClickListener(this);

        Log.d("Player status:", ProfileManager.getPlayerStatus().toString());
        switch (ProfileManager.getPlayerStatus()) {
            case IDLE: {
                stopSearching();
                break;
            }
            case IN_QUEUE_AS_PLAYER: {
                searchAs(Player.Role.PLAYER);
                break;
            }
            case IN_QUEUE_AS_LEADER: {
                searchAs(Player.Role.LEADER);
                break;
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_as_player_button: {
                searchAs(Player.Role.PLAYER);
                break;
            }
            case R.id.search_as_leader_button: {
                searchAs(Player.Role.LEADER);
                break;
            }
            case R.id.stop_searching_button: {
                stopSearching();
                break;
            }
        }
    }

    private void stopSearching() {
        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);
        RequestMaker.deleteFromQueue(ProfileManager.getPlayer().getId());

        final Button stopSearching = (Button) findViewById(R.id.stop_searching_button);
        final TextView textView = (TextView) findViewById(R.id.queueing_status_text);
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progress_bar);
        spinner.setVisibility(View.GONE);
        stopSearching.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
    }

    private void searchAs(Player.Role role) {
        switch (role) {
            case PLAYER: {
                ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IN_QUEUE_AS_PLAYER);
                RequestMaker.findBattle(Player.Role.PLAYER, ProfileManager.getPlayer().getId());
                break;
            }
            case LEADER: {
                ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IN_QUEUE_AS_LEADER);
                RequestMaker.findBattle(Player.Role.LEADER, ProfileManager.getPlayer().getId());
                break;
            }
        }

        final Button stopSearching = (Button) findViewById(R.id.stop_searching_button);
        final TextView textView = (TextView) findViewById(R.id.queueing_status_text);
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progress_bar);
        String text = role == Player.Role.PLAYER ? getString(R.string.search_as_player)
                                                 : getString(R.string.search_as_leader);
        textView.setText(text);
        textView.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        stopSearching.setVisibility(View.VISIBLE);
    }

    public void searchAgain(boolean declined, Player.Role role) {
        if (declined) {
            stopSearching();
        } else {
            searchAs(role);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Search", "onResume");
        switch(ProfileManager.getPlayerStatus()) {
            case IDLE: {
                stopSearching();
                break;
            }
            case IN_QUEUE_AS_LEADER: {
                searchAs(Player.Role.LEADER);
                break;
            }
            case IN_QUEUE_AS_PLAYER: {
                searchAs(Player.Role.PLAYER);
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
