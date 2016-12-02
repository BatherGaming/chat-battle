package ru.spbau.shevchenko.chatbattle.backend;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.frontend.BasicActivity;

public class SearcherService extends IntentService {

    private int id;

    public SearcherService() {
        super("SearcherService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        id = ProfileManager.getPlayer().getId();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while (true) {
            RequestMaker.checkIfFound(id, checkIfFoundCallback);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private RequestCallback checkIfFoundCallback = new RequestCallback() {

        @Override
        public void run(String response) {
            try {
                if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.CHATTING_AS_LEADER)
                    return;
                if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.CHATTING_AS_PLAYER)
                    return;
                JSONObject playerObject = new JSONObject(response);
                if (playerObject.has("error")) {
                    // TODO : do smth
                    return;
                }
                String chatId = playerObject.getString("chatId");
                ProfileManager.PlayerStatus status = ProfileManager.PlayerStatus.valueOf(playerObject.getString("status"));


                if (!chatId.equals("null")) {
                    int chatIdInt = Integer.valueOf(chatId);
                    ProfileManager.getPlayer().setChatId(chatIdInt);
                    BasicActivity currentActivity = ((MyApplication) getApplicationContext()).getCurrentActivity();
                    currentActivity.getBattleFoundHandler().postDelayed(
                            new BasicActivity.battleFoundRunnable(
                                    status == ProfileManager.PlayerStatus.CHATTING_AS_LEADER ? Player.Role.LEADER : Player.Role.PLAYER,
                                    currentActivity.getFragmentManager()),
                            BasicActivity.BATTLE_FOUND_HANDLE_DELAY);
                }
            } catch (JSONException e) {
                Log.e("chIfFoCallb.run", e.getMessage());
            }
        }
    };

}
