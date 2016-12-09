package ru.spbau.shevchenko.chatbattle.frontend;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        final Button sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);

        messageInput = (EditText) findViewById(R.id.message_input);
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        final ListView messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);
    }

    @Override
    public void onClick(View view) {
        postMessage(view);
    }


}