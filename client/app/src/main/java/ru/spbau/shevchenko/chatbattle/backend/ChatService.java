package ru.spbau.shevchenko.chatbattle.backend;


import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public void sendMessage(String messageText, String whiteboard) {
        JSONObject jsonMessage;
        try {
            jsonMessage = new JSONObject().put("authorId", ProfileManager.getPlayer().getId())
                    .put("text", messageText)
                    .put("chatId", ProfileManager.getPlayer().getChatId())
                    .put("whiteboard", whiteboard);
        } catch (JSONException e) {
            Log.e("sendMessage()", e.getMessage()); // TODO: handle this
            return;
        }

        RequestMaker.sendMessage(jsonMessage.toString());
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
                Message message = Message.fromJSON(jsonMessage);
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
                if (unbinded)
                    return;
                pullMessages();
            }
        };
        // Schedule first chat messages update
        handler = new Handler();
        handler.postDelayed(getMessagesRunnable, UPDATE_DELAY);

        RequestMaker.getPlayersIds(ProfileManager.getPlayer().getChatId(), getPlayersIdsCallback);
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

    private static class GetWhiteboardCallback implements RequestCallback{
        private final File whiteboard;
        private final RequestCallback callback;

        private GetWhiteboardCallback(File whiteboard, RequestCallback callback) {
            this.whiteboard = whiteboard;
            this.callback = callback;
        }

        @Override
        public void run(String response) {
            if (response.isEmpty()){
                Log.e("getWhUri", "Failed to fetch whiteboard");
                return;
            }
            try {
                FileOutputStream whiteboardOutStream = new FileOutputStream(whiteboard);
                byte[] fetchedWhiteboard = Base64.decode(response, Base64.DEFAULT);

                whiteboardOutStream.write(fetchedWhiteboard);
                whiteboardOutStream.close();
            } catch (IOException e) {
                Log.e("getWhUri", "Failed to create/write to whiteboard file");
            }
            callback.run("");
        }
    }

    public static Uri getWhiteboardURI(final String whiteboardTag, final RequestCallback callback){
        final File whiteboard = new File(MyApplication.storageDir, whiteboardTag);
        if (whiteboard.exists()){
            return Uri.fromFile(whiteboard);
        }
        RequestMaker.getWhiteboard(whiteboardTag, new GetWhiteboardCallback(whiteboard, callback));
        return null;
    }
}
