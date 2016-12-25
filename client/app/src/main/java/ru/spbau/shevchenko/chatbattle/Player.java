package ru.spbau.shevchenko.chatbattle;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.backend.StringConstants;

public class Player {

    final private int id;
    final private String login;
    final private Sex sex;
    final private int age;
    private int rating;
    private int chatId;

    public Player(int id, String login, int age, Sex sex, int chatId, int rating) {
        this.id = id;
        this.login = login;
        this.age = age;
        this.sex = sex;
        this.chatId = chatId;
        this.rating = rating;
    }

    public enum Sex {
        MALE, FEMALE;
        public String toString() {
            switch (this) {
                case MALE: return StringConstants.getMALE();
                case FEMALE: return StringConstants.getFEMALE();
            }
            throw new IllegalArgumentException();
        }
    }

    public enum Role {
        PLAYER, LEADER;
    }

    public static Player fromJSON(JSONObject playerObject) throws JSONException {
        try {
            return new Player(playerObject.getInt("id"),
                    playerObject.getString("login"),
                    playerObject.getInt("age"),
                    Sex.valueOf(playerObject.getString("sex")),
                    playerObject.optInt("chatId", -1),
                    playerObject.getInt("rating")
            );
        } catch (JSONException e) {
            Log.e("Player.fromJSON", "Failed to initialize Player from JSON: " + e.getMessage());
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

    public Sex getSex() {
        return sex;
    }

    public int getAge() {
        return age;
    }
}
