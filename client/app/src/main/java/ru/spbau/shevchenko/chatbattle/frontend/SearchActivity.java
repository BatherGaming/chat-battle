package ru.spbau.shevchenko.chatbattle.frontend;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

        final ImageButton searchAsPlayerButton = (ImageButton) findViewById(R.id.search_as_player_button);
        searchAsPlayerButton.setOnClickListener(this);

        final ImageButton searchAsLeaderButton = (ImageButton) findViewById(R.id.search_as_leader_button);
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
        ImageButton leaderButton = (ImageButton) findViewById(R.id.search_as_leader_button);
        ImageButton playerButton = (ImageButton) findViewById(R.id.search_as_player_button);

        switch (view.getId()) {
            case R.id.search_as_player_button: {
                move(leaderButton, R.anim.left_big_move);
                move(playerButton, R.anim.left_small_move);
                leaderButton.setClickable(false);
                playerButton.setClickable(false);
                searchAs(Player.Role.PLAYER);
                break;
            }
            case R.id.search_as_leader_button: {
                move(leaderButton, R.anim.right_small_move);
                move(playerButton, R.anim.right_bit_move);
                leaderButton.setClickable(false);
                playerButton.setClickable(false);
                searchAs(Player.Role.LEADER);
                break;
            }
            case R.id.stop_searching_button: {
                leaderButton.setClickable(true);
                playerButton.setClickable(true);
                leaderButton.clearAnimation();
                playerButton.clearAnimation();
                stopSearching();
                break;
            }
        }
    }

    private void stopSearching() {
        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);
        RequestMaker.deleteFromQueue(ProfileManager.getPlayer().getId());

        final Button stopSearching = (Button) findViewById(R.id.stop_searching_button);
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progress_bar);
        spinner.setVisibility(View.INVISIBLE);
        stopSearching.setVisibility(View.INVISIBLE);
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
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progress_bar);
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


    public void move(ImageView image, int anim){
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), anim);
        image.startAnimation(animation1);
    }
}
