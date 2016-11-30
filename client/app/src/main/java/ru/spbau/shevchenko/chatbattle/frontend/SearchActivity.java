package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.BattleSearcher;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final Button search_as_player_button = (Button) findViewById(R.id.search_as_player_button);
        search_as_player_button.setOnClickListener(this);

        final Button search_as_leader_button = (Button) findViewById(R.id.search_as_leader_button);
        search_as_leader_button.setOnClickListener(this);

    }

    public void onBattleFound(int battleId, Player.Role role) {
        Intent intent = role == Player.Role.PLAYER ?
                new Intent(this, ChatActivity.class) :
                new Intent(this, LeaderActivity.class);
        intent.putExtra("chatId", battleId);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_as_player_button: {
                BattleSearcher.findBattle(this, Player.Role.PLAYER);
                break;
            }
            case R.id.search_as_leader_button: {
                BattleSearcher.findBattle(this, Player.Role.LEADER);
            }
        }
    }
}
