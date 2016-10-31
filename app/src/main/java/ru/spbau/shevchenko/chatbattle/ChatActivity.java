package ru.spbau.shevchenko.chatbattle;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;




public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    EditText messageInput;
    Button sendButton;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageInput = (EditText)findViewById(R.id.message_input);
        sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        final ListView messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

    }

    public void postMessage(View view)  {
        String message = messageInput.getText().toString();
        Chatter.sendMessage(this, message);
    }

    public void update(Message message) {
        messageAdapter.add(message);
    }


    @Override
    public void onClick(View view) {
        Log.e("My app", "gg");
        postMessage(view);
    }
}