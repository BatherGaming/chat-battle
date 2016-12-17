package ru.spbau.shevchenko.chatbattle;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    // TODO: add fromJSON

    public Message(int id, String text, int authorId, int chatId, String tag) {
        this.id = id;
        this.text = text;
        this.authorId = authorId;
        this.chatId = chatId;
        if (tag == null) {
            tag = "";
        }
        this.tag = tag;
    }

    final private int id;
    final private String text;
    final private int authorId;
    final private int chatId;
    final private String tag;

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
}
