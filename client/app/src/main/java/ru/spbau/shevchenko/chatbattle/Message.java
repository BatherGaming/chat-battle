package ru.spbau.shevchenko.chatbattle;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    final private int id;
    final private String text;
    final private int authorId;
    final private int chatId;
    final private String tag;
    private Status status;

    public enum Status {DELIVERED, SENDING, FAILED}

    public Message(int id, String text, int authorId, int chatId, String tag) {
        this.id = id;
        this.text = text;
        this.authorId = authorId;
        this.chatId = chatId;
        this.tag = tag == null ? "" : tag;
        status = Status.DELIVERED;
    }

    public int getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public String getText() {
        return text;
    }

    public int getAuthorId() {
        return authorId;
    }

    public int getChatId() {
        return chatId;
    }

    public static Message fromJSON(JSONObject jsonMessage) throws JSONException {
        try {
            return new Message(jsonMessage.getInt("id"), jsonMessage.getString("text"),
                    jsonMessage.getInt("authorId"),
                    jsonMessage.getInt("chatId"),
                    (jsonMessage.isNull("whiteboardTag") ? "" : jsonMessage.getString("whiteboardTag")));
        } catch (JSONException e) {
            Log.e("Message.fromJSON", "Failed to initialize message from JSON: " + e.getMessage());
            throw e;
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
