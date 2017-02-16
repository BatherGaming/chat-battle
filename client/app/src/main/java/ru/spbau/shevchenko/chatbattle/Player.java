package ru.spbau.shevchenko.chatbattle;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Player {
    private final int id;
    private final String login;
    private int rating;
    private int chatId;

    public Player(int id, String login, int chatId, int rating) {
        this.id = id;
        this.login = login;
        this.chatId = chatId;
        this.rating = rating;
    }

    public enum Role {
        PLAYER, LEADER
    }

    public static Player fromJSON(JSONObject playerObject) throws JSONException {
        try {
            return new Player(playerObject.getInt("id"),
                    playerObject.getString("login"),
                    playerObject.optInt("chatId", -1),
                    playerObject.getInt("rating")
            );
        } catch (JSONException e) {
            Log.e("Player.fromJSON", "Failed to initialize player from JSON: " + e.getMessage());
            throw e;
        }
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

}
