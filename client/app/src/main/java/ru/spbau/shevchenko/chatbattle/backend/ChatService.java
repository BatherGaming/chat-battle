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
    private static final long UPDATE_DELAY = 1000; // milliseconds
    private int chatId = -1;
    private int messageCount;
    private ArrayList<Message> messages;

    private static Handler handler;
    private static Runnable getMessagesRunnable;

    private final IBinder chatBinder = new ChatBinder();

    public class ChatBinder extends Binder{
        public ChatService getChatService(){
            return ChatService.this;
        }
    }

    private void pullMessages() {
        RequestMaker.sendRequest(RequestMaker.domainName + "/chat/get/" + chatId + "/" + messageCount,
                RequestMaker.Method.GET,
                new RequestCallback() {
                    @Override
                    public void run(String response) {
                        onServerResponse(response);
                    }
                });
    }

    public void sendMessage(String messageText) {
        Message message = new Message(messageText, ProfileManager.getPlayer().id, chatId);
        RequestMaker.sendRequest(RequestMaker.domainName + "/chat/send", RequestMaker.Method.POST,
                new RequestCallback() {
                    @Override
                    public void run(String response) {
                        // TODO: fill this
                    }
                });
    }
    public ArrayList<Message> getMessages(){
        return messages;
    }

    private void onServerResponse(String response){
        try {
            JSONArray jsonMessages = new JSONArray(response);
            // TODO: complete
            for (int i = 0; i < jsonMessages.length(); i++) {
                JSONObject jsonMessage = jsonMessages.getJSONObject(i);
                Message message = new Message(jsonMessage.getString("text"),
                                              jsonMessage.getInt("authorId"),
                                              chatId);
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
        if (chatId != -1){
            throw new RuntimeException("More than one client trying to bind to ChatService.");
        }
        if (!intent.hasExtra("chatId")) {
            throw new RuntimeException("Binding to ChatService without providin chat id.");
        }
        chatId = intent.getIntExtra("chatId", -1);
        if (chatId == -1) {
            throw new RuntimeException("Binded to ChatService without providing chat id.");
        }

        messageCount = 0;

        getMessagesRunnable = new Runnable() {
            @Override
            public void run() {
                pullMessages();
            }
        };
        // Schedule first chat messages update
        handler = new Handler();
        handler.postDelayed(getMessagesRunnable, UPDATE_DELAY );

        return chatBinder;
    }
}