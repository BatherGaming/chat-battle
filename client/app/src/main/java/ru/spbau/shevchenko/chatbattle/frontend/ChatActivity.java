package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private ServiceConnection chatServiceConection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(ChatActivity.this, "ChatService connected!",
                    Toast.LENGTH_LONG).show();
            chatService = ((ChatService.ChatBinder) service).getChatService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            chatService = null;
        }
    };
    protected ChatService chatService = null;

    private int chatId;

    protected EditText messageInput;
    protected MessageAdapter messageAdapter;

    protected final static long HANDLER_DELAY = 100;
    private int alreadyRead = 0;

    protected Handler handler = new Handler();
    protected Runnable getMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            if (chatService != null) {
                List<Message> messages = chatService.getMessages();
                StringBuilder messagesString = new StringBuilder();
                for (Message message : messages) {
                    messagesString.append(message.text);
                }
                //Log.d("getMessagesRunnable", messagesString.toString());
                for (Message message : messages.subList(alreadyRead, messages.size())) {
                    update(message);
                }
                alreadyRead = messages.size();
            }
            handler.postDelayed(this, HANDLER_DELAY);
        }
    };

    protected Handler isFinishedHandler = new Handler();
    protected Runnable isFinishedRunnable = new Runnable() {
        @Override
        public void run() {
            RequestMaker.sendRequest(RequestMaker.domainName + "/chat/isfinished/" + ProfileManager.getPlayer().id + "/" + chatId, RequestMaker.Method.GET, new RequestCallback() {
                @Override
                public void run(String response) {
                    try {
                        JSONObject playerObject = new JSONObject(response);
                        if (playerObject.has("error")) {
                            Log.d("ChatAct.iFHandler.run", playerObject.getString("error"));
                            return;
                        }
                        String result = playerObject.getString("result");
                        if (result.equals("running")) {
                            isFinishedHandler.postDelayed(isFinishedRunnable, HANDLER_DELAY);
                        } else {
                            if (!result.equals("leader")) {
                                Toast.makeText(ChatActivity.this, result, Toast.LENGTH_LONG).show();
                            }
                            finish();
                        }
                    } catch (JSONException e) {
                        Log.e("ChatAct.iFHandler.run", e.getMessage());
                    }
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        chatId = intent.getIntExtra("chatId", -1);
        if (chatId == -1) {
            throw new RuntimeException("Created ChatActivity without providing chat id.");
        }

        Intent chatServiceIntent = new Intent(this, ChatService.class);
        chatServiceIntent.putExtra("chatId", chatId);
        bindService(chatServiceIntent, chatServiceConection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_chat);
        messageInput = (EditText)findViewById(R.id.message_input);
        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        final ListView messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);
        handler.postDelayed(getMessagesRunnable, HANDLER_DELAY);
        isFinishedHandler.postDelayed(isFinishedRunnable, HANDLER_DELAY);
    }

    public void postMessage(View view)  {
        String message = messageInput.getText().toString();
        messageInput.setText("");
        chatService.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy()", "called");
        stopService();
    }

    protected void stopService() {
        handler.removeCallbacks(getMessagesRunnable);
        isFinishedHandler.removeCallbacks(isFinishedRunnable);
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