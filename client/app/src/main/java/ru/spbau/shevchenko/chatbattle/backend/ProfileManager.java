package ru.spbau.shevchenko.chatbattle.backend;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.frontend.LoginActivity;
import ru.spbau.shevchenko.chatbattle.frontend.SignupActivity;

public class ProfileManager {
    private static Player currentPlayer = null;
    public static void signin(String login, String password, final LoginActivity loginActivity) {
        RequestMaker.sendRequest(RequestMaker.domainName + "/signin/" + login + "/" + password, RequestMaker.Method.GET, 
            new RequestCallback() {
                @Override
                public void run(String response) {
                    onSigninResponse(response, loginActivity);
                }
            });
    }
    public static void signup(Player newPlayer, String password, final SignupActivity signupActivity) {
        JSONObject jsonPlayer;
        try {
            jsonPlayer = new JSONObject().put("login", newPlayer.login)
                    .put("sex", newPlayer.sex.toString())
                    .put("age", newPlayer.age)
                    .put("password", password);
        }
        catch (JSONException e) {
            Log.e("signup()", e.getMessage()); // TODO: handle this
            return;
        }
        RequestMaker.sendRequest(RequestMaker.domainName + "/players", RequestMaker.Method.POST, new RequestCallback() {
            @Override
            public void run(String response) {
                onSignupResponse(response, signupActivity);
            }
        }, jsonPlayer.toString());
    }

    private static void onSignupResponse(String response, SignupActivity signupActivity) {
        try {
            Log.d("onSignupResponse()", response);
            JSONObject playerObject = new JSONObject(response);
            if (playerObject.has("error")) {
                signupActivity.failedSignup(playerObject.getString("error"));
                return;
            }
            currentPlayer = new Player(playerObject.getInt("id"),
                    playerObject.getString("login"),
                    playerObject.getInt("age"),
                    Player.Sex.fromString(playerObject.getString("sex"))
            );
            signupActivity.completeSignup();
        }
        catch (Exception e){
            Log.e("onSignupResponse()", e.getMessage());
            signupActivity.failedSignup(e.getMessage()); // TODO: change this somehow
        }
    }

    public static Player getPlayer() {
        // TODO: deal with possible null values
        return currentPlayer;
    }
    public static void onSigninResponse(String response, LoginActivity loginActivity) {
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
            Log.e("onSigninResponse()", e.getMessage());
            loginActivity.failedLogin(e.getMessage()); // TODO: change this somehow
        }
    }
}
