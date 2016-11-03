package ru.spbau.shevchenko.chatbattle.backend;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.spbau.shevchenko.chatbattle.Player;

/**
 * Created by ilya on 11/1/16.
 */

public class ProfileManager {
    private static Player currentPlayer = null;
    public static Player getPlayerInfo() {
        // TODO: deal with possible null values
        return currentPlayer;
    }
    public static void onServerResponse(String response){
        try {
            JSONObject playerObject = new JSONObject(response);
            currentPlayer = new Player(playerObject.getString("login"),
                                       playerObject.getInt("age"),
                                       Player.Sex.fromString(playerObject.getString("sex")));
        }
        catch (Exception e){
            Log.e("onServerResponse()", e.getMessage());
        }
    }
}
