package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;

public class LeaderActivity extends AbstractChat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.CHATTING_AS_LEADER);
    }

    public void initLayout() {
        setContentView(R.layout.activity_leader);

        final ImageButton sendButton = (ImageButton) findViewById(R.id.send_button);
        final ImageButton whiteboardButton = (ImageButton) findViewById(R.id.whiteboard_btn);
        final Button chooseButton = (Button) findViewById(R.id.leader_choose_button);
        sendButton.setOnClickListener(this);
        chooseButton.setOnClickListener(this);
        whiteboardButton.setOnClickListener(this);

        messageInput = (EditText) findViewById(R.id.leader_message_input);
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        final ListView messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.leader_choose_button: {
                Intent intent = new Intent(this, WinnerPickActivity.class);
                intent.putExtra("playersId", chatService.getPlayersId());
                startActivity(intent);
                break;
            }
            default: {
                super.onClick(view);
            }
        }
    }
}
