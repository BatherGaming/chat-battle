package ru.spbau.shevchenko.chatbattle.backend;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.frontend.BasicActivity;
import ru.spbau.shevchenko.chatbattle.frontend.LoginActivity;
import ru.spbau.shevchenko.chatbattle.frontend.SignupActivity;

import static ru.spbau.shevchenko.chatbattle.frontend.BasicActivity.PREFS_FILE_NAME;

public class ProfileManager {
    private static Player currentPlayer = null;
    private static PlayerStatus playerStatus;

    public enum PlayerStatus {
        IDLE, IN_QUEUE_AS_LEADER, IN_QUEUE_AS_PLAYER, CHATTING_AS_LEADER, CHATTING_AS_PLAYER, WAITING
    }

    public static PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public static void setPlayerStatus(PlayerStatus newPlayerStatus) {
        playerStatus = newPlayerStatus;
    }

    public static void signIn(String login, String password, final LoginActivity loginActivity) {
        RequestMaker.signIn(password, login, new SignInCallback(loginActivity));
    }

    public static void signUp(Player newPlayer, String password, String email, final SignupActivity signupActivity) {
        final JSONObject jsonPlayer;
        try {
            jsonPlayer = new JSONObject().put("login", newPlayer.getLogin())
                    .put("password", password)
                    .put("email", email);
        } catch (JSONException e) {
            Log.e("signUp()", e.getMessage()); // TODO: handle this
            return;
        }


        RequestMaker.signUp(jsonPlayer.toString(), new SignUpCallback(signupActivity));
    }

    public static void setCurrentPlayer(Player player) {
        currentPlayer = player;
    }

    public static Player getPlayer() {
        return currentPlayer;
    }

    private static void onSignInResponse(RequestResult requestResult, LoginActivity loginActivity) {
        if (requestResult.getStatus() != RequestResult.Status.OK) {
            loginActivity.loginResponse(requestResult.getStatus());
            return;
        }
        try {
            final JSONObject playerObject = new JSONObject(requestResult.getResponse());
            if (playerObject.has("error")) {
                loginActivity.failedLogin(playerObject.getString("error"));
                return;
            }
            currentPlayer = Player.fromJSON(playerObject);
            playerStatus = PlayerStatus.valueOf(playerObject.getString("status"));
            loginActivity.completeLogin();
        } catch (Exception e) {
            Log.e("onSignInResponse()", e.getMessage());
            loginActivity.loginResponse(RequestResult.Status.ERROR); // TODO: change this somehow
        }
    }

    private static void onSignUpResponse(RequestResult requestResult, SignupActivity signupActivity) {
        if (requestResult.getStatus() != RequestResult.Status.OK) {
            signupActivity.signupResponse(requestResult.getStatus());

        }
        try {
            final JSONObject playerObject = new JSONObject(requestResult.getResponse());
            if (playerObject.has("error")) {
                signupActivity.failedSignup(playerObject.getString("error"));
                return;
            }
            currentPlayer = Player.fromJSON(playerObject);
            playerStatus = PlayerStatus.valueOf(playerObject.getString("status"));
            signupActivity.completeSignup();
        } catch (Exception e) {
            Log.e("onSignUpResponse()", e.getMessage());
            signupActivity.signupResponse(RequestResult.Status.ERROR);
        }
    }

    private static class SignInCallback implements RequestCallback {
        private LoginActivity loginActivity;

        SignInCallback(LoginActivity loginActivity) {
            this.loginActivity = loginActivity;
        }

        @Override
        public void run(RequestResult requestResult) {
            onSignInResponse(requestResult, loginActivity);
        }
    }

    private static class SignUpCallback implements RequestCallback {
        private SignupActivity signupActivity;

        SignUpCallback(SignupActivity signupActivity) {
            this.signupActivity = signupActivity;
        }

        @Override
        public void run(RequestResult requestResult) {
            onSignUpResponse(requestResult, signupActivity);
        }
    }
}
