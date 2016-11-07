package ru.spbau.shevchenko.chatbattle.backend;

import android.util.Log;

import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.frontend.LoginActivity;

/**
 * Created by ilya on 11/1/16.
 */

public class ProfileManager {
    private static Player currentPlayer = null;
    public static void signin(String login, String password, final LoginActivity loginActivity){
        RequestMaker.sendRequest("http://qwsafex.pythonanywhere.com/signin/"+login+"/"+password,
                RequestMaker.Method.GET, new RequestCallback() {
            @Override
            public void run(String response) {
                ProfileManager.onLoginResponse(response, loginActivity);
            }
        });
    }
    public static Player getPlayer() {
        // TODO: deal with possible null values
        return currentPlayer;
    }
    private static void onLoginResponse(String response, LoginActivity loginActivity){
        try {
            JSONObject playerObject = new JSONObject(response);
            if (playerObject.has("error")) {
                loginActivity.failedLogin(playerObject.getString("error"));
                return;
            }
            currentPlayer = new Player(playerObject.getInt("id"),
                                       playerObject.getString("login"),
                                       playerObject.getInt("age"),
                                       Player.Sex.fromString(playerObject.getString("sex"))
                                       );
            loginActivity.completeLogin();
        }
        catch (Exception e){
            Log.e("onLoginResponse()", e.getMessage());
            loginActivity.failedLogin(e.getMessage()); // TODO: remove this
        }
    }
}
