package ru.spbau.shevchenko.chatbattle;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Player {
    // TODO: add fromJSON
    public Player(int id, String login, int age, Sex sex, int chatId) {
        this.id = id;
        this.login = login;
        this.age = age;
        this.sex = sex;
        this.chatId = chatId;
    }

    public static Player fromJSON(JSONObject playerObject) {
        try {
            return new Player(playerObject.getInt("id"),
                    playerObject.getString("login"),
                    playerObject.getInt("age"),
                    Sex.fromString(playerObject.getString("sex")),
                    playerObject.optInt("chatId", -1)
            );
        } catch (JSONException e) {
            Log.e("Player.fromJSON", "Failed to initialize Player from JSON: " + e.getMessage());
        }
        return null;
    }

    public enum Sex {
        MALE, FEMALE;

        public static Sex fromString(String sex) {
            return Sex.valueOf(sex);
        }
    }

    public enum Role {
        PLAYER, LEADER;
    }

    final private int id;
    final private String login;
    final private Sex sex;
    final private int age;

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    private int chatId;

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
