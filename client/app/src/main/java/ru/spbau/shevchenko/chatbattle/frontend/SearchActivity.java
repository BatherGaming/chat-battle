package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.BattleSearcher;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {
    private Button search_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        search_button = (Button)findViewById(R.id.search_button);
        search_button.setOnClickListener(this);

    }

    public void onBattleFound(int battleId){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", battleId);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_button: {              
                BattleSearcher.findBattle(this);
                break;
            }
        }
    }
}
