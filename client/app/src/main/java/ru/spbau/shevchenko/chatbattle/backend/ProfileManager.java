package ru.spbau.shevchenko.chatbattle.backend;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.frontend.LoginActivity;
import ru.spbau.shevchenko.chatbattle.frontend.SignupActivity;

public class ProfileManager {
    private static Player currentPlayer = null;

    public static void signIn(String login, String password, final LoginActivity loginActivity) {
        RequestMaker.signIn(password, login, new SignInCallback(loginActivity));
    }

    public static void signUp(Player newPlayer, String password, final SignupActivity signupActivity) {
        JSONObject jsonPlayer;
        try {
            jsonPlayer = new JSONObject().put("login", newPlayer.getLogin())
                    .put("sex", newPlayer.getSex().toString())
                    .put("age", newPlayer.getAge())
                    .put("password", password);
        } catch (JSONException e) {
            Log.e("signUp()", e.getMessage()); // TODO: handle this
            return;
        }


        RequestMaker.singUp(jsonPlayer.toString(), new SignUpCallback(signupActivity));
    }

    private static void onSignUpResponse(String response, SignupActivity signupActivity) {
        try {
            Log.d("onSignUpResponse()", response);
            JSONObject playerObject = new JSONObject(response);
            if (playerObject.has("error")) {
                signupActivity.failedSignup(playerObject.getString("error"));
                return;
            }
            currentPlayer = new Player(playerObject.getInt("id"),
                    playerObject.getString("login"),
                    playerObject.getInt("age"),
                    Player.Sex.fromString(playerObject.getString("sex")),
                    playerObject.optInt("chatId", -1)
            );
            playerStatus = PlayerStatus.valueOf(playerObject.getString("status"));
            signupActivity.completeSignup();
        } catch (Exception e) {
            Log.e("onSignUpResponse()", e.getMessage());
            signupActivity.failedSignup(e.getMessage()); // TODO: change this somehow
        }
    }

    public static Player getPlayer() {
        // TODO: deal with possible null values
        return currentPlayer;
    }

    private static void onSignInResponse(String response, LoginActivity loginActivity) {
        try {
            JSONObject playerObject = new JSONObject(response);
            if (playerObject.has("error")) {
                loginActivity.failedLogin(playerObject.getString("error"));
                return;
            }
            currentPlayer = new Player(playerObject.getInt("id"),
                    playerObject.getString("login"),
                    playerObject.getInt("age"),
                    Player.Sex.fromString(playerObject.getString("sex")),
                    playerObject.optInt("chatId", -1)
            );
            playerStatus = PlayerStatus.valueOf(playerObject.getString("status"));
            loginActivity.completeLogin();
        } catch (Exception e) {
            Log.e("onSignInResponse()", e.getMessage());
            loginActivity.failedLogin(e.getMessage()); // TODO: change this somehow
        }
    }

    static private class SignInCallback implements RequestCallback {
        private LoginActivity loginActivity;

        SignInCallback(LoginActivity loginActivity) {
            this.loginActivity = loginActivity;
        }

        @Override
        public void run(String response) {
            onSignInResponse(response, loginActivity);
        }
    }

    static private class SignUpCallback implements RequestCallback {
        private SignupActivity signupActivity;

        SignUpCallback(SignupActivity signupActivity) {
            this.signupActivity = signupActivity;
        }

        @Override
        public void run(String response) {
            onSignUpResponse(response, signupActivity);
        }
    }


    public enum PlayerStatus {
        IDLE, IN_QUEUE_AS_LEADER, IN_QUEUE_AS_PLAYER, CHATTING_AS_LEADER, CHATTING_AS_PLAYER, WAITING;
    }

    static private PlayerStatus playerStatus;

    static public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    static public void setPlayerStatus(PlayerStatus newPlayerStatus) {
        playerStatus = newPlayerStatus;
    }


}
