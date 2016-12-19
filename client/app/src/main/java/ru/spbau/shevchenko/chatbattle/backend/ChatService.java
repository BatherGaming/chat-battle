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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.Message;

public class ChatService extends Service {
    private static final long UPDATE_DELAY = 100; // milliseconds
    private int messageCount;
    private ArrayList<Message> messages;
    private ArrayList<Integer> playersId = new ArrayList<>();
    private boolean unbound = false;

    private Handler handler;
    private Runnable getMessagesRunnable;

    private final IBinder chatBinder = new ChatBinder();
    private boolean messagesInitialized = false; // becomes true after first server response
    private boolean playersIdsInitialized = false; // becomes true after first server response

    public boolean initialized() {
        return messagesInitialized && playersIdsInitialized;
    }

    public class ChatBinder extends Binder {
        public ChatService getChatService() {
            return ChatService.this;
        }
    }

    private void pullMessages() {
        RequestMaker.pullMessages(ProfileManager.getPlayer().getChatId(), messageCount, pullMessagesCallback);
    }

    private class SendMessageCallback implements RequestCallback {
        private final RequestCallback callback;
        private String localWhiteboardTag;
        public SendMessageCallback(String localWhiteboardTag, RequestCallback callback) {
            this.localWhiteboardTag = localWhiteboardTag;
            this.callback = callback;
        }

        @Override
        public void run(RequestResult requestResult) {
            if (requestResult.getStatus() == RequestResult.Status.FAILED_CONNECTION) {
                // Try sending message again
                RequestMaker.sendMessage(requestResult.getResponse(), this);
                return;
            }
            copyWhiteboard(requestResult.getResponse(), localWhiteboardTag);
            callback.run(new RequestResult());
        }
    }

    public void sendMessage(String messageText, String whiteboard, final String localWhiteboardTag, RequestCallback callback) {
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

        RequestMaker.sendMessage(jsonMessage.toString(), new SendMessageCallback(localWhiteboardTag, callback));
    }

    private void copyWhiteboard(String response, String localWhiteboardTag) {
        if (localWhiteboardTag.isEmpty()){
            return;
        }
        try {
            Message message = Message.fromJSON(new JSONObject(response));
            File localWhiteboard = new File(MyApplication.storageDir, localWhiteboardTag);
            File globalWhiteboard = new File(MyApplication.storageDir, message.getTag());

            // Copy whiteboard for better caching
            InputStream in = new FileInputStream(localWhiteboard);
            OutputStream out = new FileOutputStream(globalWhiteboard);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (JSONException | IOException e) {
            Log.d("onMessageDeliver", e.getMessage());
        }
    }


    public ArrayList<Message> getMessages() {
        return messages;
    }

    public ArrayList<Integer> getPlayersId() {
        return playersId;
    }

    private void onServerResponse(RequestResult requestResult) {
        try {
            JSONArray jsonMessages = new JSONArray(requestResult.getResponse());
            // TODO: complete
            for (int i = 0; i < jsonMessages.length(); i++) {
                JSONObject jsonMessage = jsonMessages.getJSONObject(i);
                Message message = Message.fromJSON(jsonMessage);
                messages.add(message);
            }
            messageCount += jsonMessages.length();

            messagesInitialized = true;
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
                if (unbound)
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
        unbound = true;
        handler.removeCallbacks(getMessagesRunnable);
        return false;
    }

    final private RequestCallback pullMessagesCallback = new RequestCallback() {
        @Override
        public void run(RequestResult requestResult) {
            onServerResponse(requestResult);
        }
    };


    final RequestCallback getPlayersIdsCallback = new RequestCallback() {
        @Override
        public void run(RequestResult requestResult) {
            try {
                JSONArray jsonMessages = new JSONArray(requestResult.getResponse());
                for (int i = 0; i < jsonMessages.length(); i++) {
                    JSONObject jsonPlayer = jsonMessages.getJSONObject(i);
                    playersId.add(jsonPlayer.getInt("id"));
                }
                playersIdsInitialized = true;
            } catch (JSONException e) {
                Log.e("ChSe.getPlayersId", e.getMessage());
            }
        }
    };

    public static void saveWhiteboard(File destination, String content){ // TODO: move to another class?
        try {
            FileOutputStream whiteboardOutStream = new FileOutputStream(destination);
            byte[] fetchedWhiteboard = Base64.decode(content, Base64.DEFAULT);

            whiteboardOutStream.write(fetchedWhiteboard);
            whiteboardOutStream.close();
        } catch (IOException e) {
            Log.e("saveWhiteboard", "Failed to create/write to whiteboard file");
        }
    }

    private static class GetWhiteboardCallback implements RequestCallback{
        private final File whiteboard;
        private final RequestCallback callback;

        private GetWhiteboardCallback(File whiteboard, RequestCallback callback) {
            this.whiteboard = whiteboard;
            this.callback = callback;
        }

        @Override
        public void run(RequestResult requestResult) {
            String response = requestResult.getResponse();
            if (response.isEmpty()) {
                Log.e("getWhUri", "Failed to fetch whiteboard");
                return;
            }
            saveWhiteboard(whiteboard, response);
            callback.run(new RequestResult());
        }
    }

    public static Uri getWhiteboardURI(final String whiteboardTag, RequestCallback callback) {
        final File whiteboard = new File(MyApplication.storageDir, whiteboardTag);
        if (whiteboard.exists()) {
            return Uri.fromFile(whiteboard);
        }
        RequestMaker.getWhiteboard(whiteboardTag, new GetWhiteboardCallback(whiteboard, callback));
        return null;
    }
}
