package ru.spbau.shevchenko.chatbattle.frontend;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;


public class PlayerActivity extends AbstractChat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.CHATTING_AS_PLAYER);
    }

    @Override
    public void initLayout() {
        setContentView(R.layout.activity_player);
    }


}