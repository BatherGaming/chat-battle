package ru.spbau.shevchenko.chatbattle;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private final int id;
    private final String text;
    private final int authorId;
    private final String tag;
    private Status status;

    public Message(int id, String text, int authorId, String tag) {
        this.id = id;
        this.text = text;
        this.authorId = authorId;
        this.tag = tag == null ? "" : tag;
        status = Status.DELIVERED;
    }

    public enum Status {DELIVERED, SENDING, FAILED}

    public static Message fromJSON(JSONObject jsonMessage) throws JSONException {
        try {
            return new Message(jsonMessage.getInt("id"), jsonMessage.getString("text"),
                    jsonMessage.getInt("authorId"),
                    (jsonMessage.isNull("whiteboardTag") ? "" : jsonMessage.getString("whiteboardTag")));
        } catch (JSONException e) {
            Log.e("Message.fromJSON", "Failed to initialize message from JSON: " + e.getMessage());
            throw e;
        }
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
