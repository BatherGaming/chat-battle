package ru.spbau.shevchenko.chatbattle.backend;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.Message;

public class ChatService extends Service {
    private static final long UPDATE_DELAY = 100; // milliseconds
    private int messageCount;
    private ArrayList<Message> messages;
    private ArrayList<Integer> playersId = new ArrayList<>();
    private boolean unbinded = false;

    private Handler handler;
    private Runnable getMessagesRunnable;

    private final IBinder chatBinder = new ChatBinder();

    public class ChatBinder extends Binder {
        public ChatService getChatService() {
            return ChatService.this;
        }
    }

    private void pullMessages() {
        RequestMaker.pullMessages(ProfileManager.getPlayer().getChatId(), messageCount, pullMessagesCallback);
    }

    public void sendMessage(String messageText) {
        JSONObject jsonMessage;
        try {
            jsonMessage = new JSONObject().put("authorId", ProfileManager.getPlayer().getId())
                    .put("text", messageText)
                    .put("chatId", ProfileManager.getPlayer().getChatId());
        } catch (JSONException e) {
            Log.e("sendMessage()", e.getMessage()); // TODO: handle this
            return;
        }

        RequestMaker.sendMessage(sendMessageCallback, jsonMessage.toString());
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public ArrayList<Integer> getPlayersId() {
        return playersId;
    }

    private void onServerResponse(String response) {
        try {
            JSONArray jsonMessages = new JSONArray(response);
            // TODO: complete
            for (int i = 0; i < jsonMessages.length(); i++) {
                JSONObject jsonMessage = jsonMessages.getJSONObject(i);
                Message message = new Message(jsonMessage.getString("text"),
                        jsonMessage.getInt("authorId"),
                        ProfileManager.getPlayer().getChatId());
                messages.add(message);
            }
            messageCount += jsonMessages.length();

            // Schedule next chat messages update
            handler.postDelayed(getMessagesRunnable, UPDATE_DELAY);
        } catch (JSONException e) {
            Log.e("Chatter", e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        messageCount = 0;
        messages = new ArrayList<>();
        getMessagesRunnable = new Runnable() {
            @Override
            public void run() {
                //  Log.d("getMRunnable", "running");
                if (unbinded)
                    return;
                pullMessages();
            }
        };
        // Schedule first chat messages update
        handler = new Handler();
        handler.postDelayed(getMessagesRunnable, UPDATE_DELAY);

        RequestMaker.getPlayersIds(getPlayersIdsCallback, ProfileManager.getPlayer().getChatId());
        return chatBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("onUnbind", "called");
        unbinded = true;
        handler.removeCallbacks(getMessagesRunnable);
        return false;
    }

    final private RequestCallback pullMessagesCallback = new RequestCallback() {
        @Override
        public void run(String response) {
            onServerResponse(response);
        }
    };

    final private RequestCallback sendMessageCallback = new RequestCallback() {
        @Override
        public void run(String response) {
            // TODO: fill this
        }
    };

    final RequestCallback getPlayersIdsCallback = new RequestCallback() {
        @Override
        public void run(String response) {
            try {
                JSONArray jsonMessages = new JSONArray(response);
                for (int i = 0; i < jsonMessages.length(); i++) {
                    JSONObject jsonPlayer = jsonMessages.getJSONObject(i);
                    playersId.add(jsonPlayer.getInt("id"));
                }
            } catch (JSONException e) {
                Log.e("ChSe.getPlayersId", e.getMessage());
            }
        }
    };
}
