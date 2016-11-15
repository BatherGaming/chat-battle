package ru.spbau.shevchenko.chatbattle.frontend;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.spbau.shevchenko.chatbattle.backend.Chatter;
import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText messageInput;
    private Button sendButton;
    private MessageAdapter messageAdapter;

    private final static long HANDLER_DELAY = 2000;
    private int alreadyRead = 0;

    private Handler handler = new Handler();
    private Runnable getMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            List<Message> messages = Chatter.getMessages();
            for (Message message : messages.subList(alreadyRead, messages.size())) {
                update(message);
            }
            alreadyRead = messages.size();
            handler.postDelayed(this, HANDLER_DELAY);
        }
    };

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
        handler.postDelayed(getMessagesRunnable, HANDLER_DELAY);
    }

    public void postMessage(View view)  {
        String message = messageInput.getText().toString();
        Chatter.sendMessage(this, message);
        messageInput.setText("");
    }


    public void update(Message message) {
        messageAdapter.add(message);
    }


    @Override
    public void onClick(View view) {
        postMessage(view);
    }
}