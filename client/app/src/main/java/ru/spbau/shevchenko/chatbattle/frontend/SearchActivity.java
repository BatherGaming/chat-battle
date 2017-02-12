package ru.spbau.shevchenko.chatbattle.frontend;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

        final TextView chatTextView = (TextView) findViewById(R.id.chat_text_view);
        chatTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/KeyCapsFLF.ttf"));

        final TextView battleTextView = (TextView) findViewById(R.id.battle_text_view);
        battleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/KeyCapsFLF.ttf"));

        final TextView descView = (TextView) findViewById(R.id.search_desc_text_view);
        descView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/IMMORTAL.ttf"));

        final Button searchAsPlayerButton = (Button) findViewById(R.id.search_as_player_button);
        searchAsPlayerButton.setOnClickListener(this);
        searchAsPlayerButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/IMMORTAL.ttf"));

        final Button searchAsLeaderButton = (Button) findViewById(R.id.search_as_leader_button);
        searchAsLeaderButton.setOnClickListener(this);
        searchAsLeaderButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/IMMORTAL.ttf"));

        final Button stopSearchingButton = (Button) findViewById(R.id.stop_searching_button);
        stopSearchingButton.setOnClickListener(this);

        Log.d("Player status:", ProfileManager.getPlayerStatus().toString());
        switch (ProfileManager.getPlayerStatus()) {
            case IDLE: {
                stopSearching();
                break;
            }
            case IN_QUEUE_AS_PLAYER: {
                searchAs(Player.Role.PLAYER, true);
                break;
            }
            case IN_QUEUE_AS_LEADER: {
                searchAs(Player.Role.LEADER, true);
                break;
            }
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.search_as_player_button: {
                searchAs(Player.Role.PLAYER, false);
                break;
            }
            case R.id.search_as_leader_button: {
                searchAs(Player.Role.LEADER, false);
                break;
            }
            case R.id.stop_searching_button: {
                stopSearching();
                break;
            }
        }
    }

    private void stopSearching() {
        final Button leaderButton = (Button) findViewById(R.id.search_as_leader_button);
        final Button playerButton = (Button) findViewById(R.id.search_as_player_button);

        leaderButton.setClickable(true);
        playerButton.setClickable(true);
        leaderButton.clearAnimation();
        playerButton.clearAnimation();

        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);
        RequestMaker.deleteFromQueue(ProfileManager.getPlayer().getId());

        final Button stopSearching = (Button) findViewById(R.id.stop_searching_button);
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progress_bar);
        spinner.setVisibility(View.INVISIBLE);
        stopSearching.setVisibility(View.INVISIBLE);
    }

    private void searchAs(Player.Role role, boolean durationZero) {
        final Button leaderButton = (Button) findViewById(R.id.search_as_leader_button);
        final Button playerButton = (Button) findViewById(R.id.search_as_player_button);

        switch (role) {
            case PLAYER: {
                move(leaderButton, R.anim.top_big_move, durationZero);
                move(playerButton, R.anim.top_small_move, durationZero);
                leaderButton.setClickable(false);
                playerButton.setClickable(false);

                ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IN_QUEUE_AS_PLAYER);
                RequestMaker.findBattle(Player.Role.PLAYER, ProfileManager.getPlayer().getId());
                break;
            }
            case LEADER: {
                move(leaderButton, R.anim.down_small_move, durationZero);
                move(playerButton, R.anim.down_big_move, durationZero);
                leaderButton.setClickable(false);
                playerButton.setClickable(false);

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
            searchAs(role, true);
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
                searchAs(Player.Role.LEADER, true);
                break;
            }
            case IN_QUEUE_AS_PLAYER: {
                searchAs(Player.Role.PLAYER, true);
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


    public void move(View image, int anim, boolean durationZero){
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), anim);
        if (durationZero) animation1.setDuration(0);
        image.startAnimation(animation1);
    }
}
