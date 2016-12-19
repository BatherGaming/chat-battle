package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;


public abstract class AbstractChat extends BasicActivity implements View.OnClickListener {

    private static final int DRAW_WHITEBOARD = 1;
    private final static long HANDLER_DELAY = 100;

    private ListView messagesView;
    private ImageButton sendBtn;

    //    abstract public void onClick(View view);
    abstract public void initLayout();

    private boolean initialized = false;

    final private ServiceConnection chatServiceConection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            chatService = ((ChatService.ChatBinder) service).getChatService();
            handler.postDelayed(getMessagesRunnable, HANDLER_DELAY);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            chatService = null;
        }
    };
    protected ChatService chatService = null;

    protected EditText messageInput;
    protected MessageAdapter messageAdapter;

    private ImageButton whiteboardBtn;
    private int alreadyRead = 0;
    private String whiteboardEncoded = "";

    final private Handler handler = new Handler();
    final private Runnable getMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            if (chatService != null) {
                final List<Message> messages = chatService.getMessages();
                for (Message message : messages.subList(alreadyRead, messages.size())) {
                    // Add our own messages only on initialization
                    if (!initialized || message.getAuthorId() != ProfileManager.getPlayer().getId()) {
                        update(message);
                    }
                }
                alreadyRead = messages.size();
                if (!initialized && chatService.initialized()){
                    initialized = true;
                    completeInitialization();
                }
            }
            handler.postDelayed(this, HANDLER_DELAY);
        }
    };

    final private Handler chatStatusHandler = new Handler();
    final private Runnable chatStatusRunnable = new Runnable() {
        @Override
        public void run() {
            RequestMaker.chatStatus(ProfileManager.getPlayer().getId(),
                    ProfileManager.getPlayer().getChatId(), chatStatusCallback);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setVisibility(View.GONE);
        sendBtn = (ImageButton) findViewById(R.id.send_button);
        sendBtn.setEnabled(false);

        if (ProfileManager.getPlayer().getChatId() == -1) {
            throw new RuntimeException("Created Chat without providing chat id.");
        }

        final Intent chatServiceIntent = new Intent(this, ChatService.class);
        bindService(chatServiceIntent, chatServiceConection, Context.BIND_AUTO_CREATE);

        whiteboardBtn = (ImageButton) findViewById(R.id.whiteboard_btn);

        chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
    }

    private void completeInitialization() {
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.initializing_progress_bar);
        spinner.setVisibility(View.GONE);
        messagesView.setVisibility(View.VISIBLE);
        sendBtn.setEnabled(true);
    }


    public void postMessage() {
        final String messageText = messageInput.getText().toString();
        messageInput.setText("");

        // Save whiteboard locally
        String whiteboardTag = "";
        if (!whiteboardEncoded.isEmpty()){
            try {
                File localWhiteboard = File.createTempFile("my_", "", MyApplication.storageDir);
                whiteboardTag = localWhiteboard.getName();
                ChatService.saveWhiteboard(localWhiteboard, whiteboardEncoded);
            } catch (IOException e) {
                Log.d("postMessage", e.getMessage());
            }
        }

        chatService.sendMessage(messageText, whiteboardEncoded, whiteboardTag);
        whiteboardEncoded = "";
        // Change to display that there's no whiteboard attached
        whiteboardBtn.setImageResource(R.drawable.whiteboard);

        // Post image to list view
        final Message message = new Message(-1, messageText, ProfileManager.getPlayer().getId(),
                ProfileManager.getPlayer().getChatId(), whiteboardTag);
        update(message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy()", "called");
        stopService();
    }

    protected void stopService() {
        handler.removeCallbacks(getMessagesRunnable);
        chatStatusHandler.removeCallbacks(chatStatusRunnable);
        unbindService(chatServiceConection);
    }

    public void update(Message message) {
        messageAdapter.add(message);
    }

    final private RequestCallback chatStatusCallback = new RequestCallback() {
        @Override
        public void run(String response) {
            try {
                final JSONObject playerObject = new JSONObject(response);
                if (playerObject.has("error")) {
                    Log.d("ChatAct.iFHandler.run", playerObject.getString("error"));
                    return;
                }
                final String result = playerObject.getString("result");
                if (result.equals("running")) {
                    chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
                } else {
                    if (!result.equals("leader")) {
                        final int newRating = playerObject.getInt("rating");
                        ProfileManager.getPlayer().setRating(newRating);
                        Toast.makeText(AbstractChat.this, result +
                                "\nNew rating is " + newRating, Toast.LENGTH_LONG).show();
                    }
                    ProfileManager.getPlayer().setChatId(-1);
                    ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);

                    finish();
                }
            } catch (JSONException e) {
                Log.e("ChatAct.iFHandler.run", e.getMessage());
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_WHITEBOARD) {
            if (resultCode != RESULT_OK) {
                return;
            }

            final byte[] whiteboardBytes = data.getByteArrayExtra("whiteboard");

            whiteboardEncoded = Base64.encodeToString(whiteboardBytes, Base64.NO_WRAP);

            // Change it to display that whiteboard is attached
            whiteboardBtn.setImageResource(R.mipmap.whiteboard_drawn);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_button: {
                postMessage();
                break;
            }
            case R.id.whiteboard_btn: {
                final Intent intent = new Intent(this, WhiteboardActivity.class);
                startActivityForResult(intent, DRAW_WHITEBOARD);
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }


}