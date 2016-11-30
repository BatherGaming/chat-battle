package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;

public class LeaderActivity extends ChatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initLayout() {
        setContentView(R.layout.activity_leader);

        final Button sendButton = (Button) findViewById(R.id.leader_send_button);
        final Button chooseButton = (Button) findViewById(R.id.leader_choose_button);
        sendButton.setOnClickListener(this);
        chooseButton.setOnClickListener(this);

        messageInput = (EditText) findViewById(R.id.leader_message_input);
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        final ListView messagesView = (ListView) findViewById(R.id.leader_messages_view);
        messagesView.setAdapter(messageAdapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.leader_send_button: {
                String message = messageInput.getText().toString();
                messageInput.setText("");
                chatService.sendMessage(message);
                break;
            }
            case R.id.leader_choose_button: {
                Intent intent = new Intent(this, WinnerPickActivity.class);
                intent.putExtra("playersId", chatService.getPlayersId());
                startActivity(intent);
            }
        }
    }
}
