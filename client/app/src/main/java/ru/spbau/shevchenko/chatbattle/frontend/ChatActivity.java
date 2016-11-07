package ru.spbau.shevchenko.chatbattle.frontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.backend.Chatter;
import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    EditText messageInput;
    MessageAdapter messageAdapter;
    Chatter chatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageInput = (EditText)findViewById(R.id.message_input);
        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        final ListView messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        chatter = new Chatter(0, this); // TODO: insert correct chat id
    }

    public void postMessage(View view)  {
        String message = messageInput.getText().toString();
        chatter.sendMessage(message);
    }

    public void update(Message message) {
        messageAdapter.add(message);
    }


    @Override
    public void onClick(View view) {
        postMessage(view);
    }
}