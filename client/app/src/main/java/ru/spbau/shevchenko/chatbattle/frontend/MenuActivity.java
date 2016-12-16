package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.SearcherService;

public class MenuActivity extends BasicActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(this);
        Button profileButton = (Button) findViewById(R.id.profileButton);
        profileButton.setOnClickListener(this);

        if (!isServiceCreated) {
            Intent intent = new Intent(this, SearcherService.class);
            startService(intent);
            isServiceCreated = true;
        }

    }

    private static boolean isServiceCreated = false;

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playButton: {
                Log.d("Status=", ProfileManager.getPlayerStatus().toString());
                switch (ProfileManager.getPlayerStatus()) {
                    case IN_QUEUE_AS_LEADER:
                    case IN_QUEUE_AS_PLAYER:
                    case IDLE: {
                        Intent intent = new Intent(this, SearchActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case CHATTING_AS_LEADER: {
                        Intent intent = new Intent(this, LeaderActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case CHATTING_AS_PLAYER: {
                        Intent intent = new Intent(this, PlayerActivity.class);
                        startActivity(intent);
                    }
                }
                break;
            }
            case R.id.profileButton: {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            }

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Class<?> previousClass = BasicActivity.getLastActivityClass();
        if ((ProfileManager.getPlayer().getChatId() == -1) && previousClass != null && AbstractChat.class.isAssignableFrom(previousClass)) {
            Log.d("My app", previousClass.getName());
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }
    }

}
