package ru.spbau.shevchenko.chatbattle.backend;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.frontend.ChatActivity;

/**
 * Created by Nikolay on 28.10.16.
 */

public class Chatter {
    private static final long UPDATE_DELAY = 1000; // milliseconds
    private int chatId;
    private int messageCount;
    private ChatActivity chatActivity;
    public Chatter(int chatId, ChatActivity chatActivity){
        this.chatId = chatId;
        this.messageCount = 0;
        this.chatActivity = chatActivity;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getMessages();
                }
            }, 0, UPDATE_DELAY);
    }

    private void getMessages() {
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

    private void onServerResponse(String response){
        try {
            JSONArray messages = new JSONArray(response);
            for (int i = 0; i < messages.length(); i++) {
                JSONObject message = messages.getJSONObject(i);
                chatActivity.update(new Message(message.getString("text"),
                                                message.getInt("authorId"),
                                                chatId));
            }
            messageCount += messages.length();
        } catch (JSONException e) {
            Log.e("Chatter", e.getMessage());
        }
    }
}
