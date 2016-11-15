package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private ServiceConnection chatServiceConection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            chatService = ((ChatService.ChatBinder) service).getChatService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            chatService = null;
        }
    };
    private ChatService chatService = null;

    EditText messageInput;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int chatId = intent.getIntExtra("chatId", -1);
        if (chatId == -1) {
            throw new RuntimeException("Created ChatActivity without providing chat id.");
        }

        Intent chatServiceIntent = new Intent(this, ChatService.class);
        chatServiceIntent.putExtra("chatId", chatId);
        bindService(intent, chatServiceConection, Context.BIND_AUTO_CREATE);


        setContentView(R.layout.activity_chat);
        messageInput = (EditText)findViewById(R.id.message_input);
        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        final ListView messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

    }

    public void postMessage(View view)  {
        String message = messageInput.getText().toString();
        chatService.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(chatServiceConection);
    }

    public void update(Message message) {
        messageAdapter.add(message);
    }


    @Override
    public void onClick(View view) {
        postMessage(view);
    }


}