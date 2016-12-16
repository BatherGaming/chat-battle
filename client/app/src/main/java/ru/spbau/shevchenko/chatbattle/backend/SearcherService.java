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
            if (!waitingCallback) {
                RequestMaker.checkIfFound(id, checkIfFoundCallback);
                waitingCallback = true;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    private boolean waitingCallback = false;

    private int lastChatId = -1;

    private RequestCallback checkIfFoundCallback = new RequestCallback() {

        @Override
        public void run(String response) {
            try {
                waitingCallback = false;
                if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.CHATTING_AS_LEADER)
                    return;
                if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.CHATTING_AS_PLAYER)
                    return;
                if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.WAITING)
                    return;

                JSONObject playerObject = new JSONObject(response);
                if (playerObject.has("error")) {
                    // TODO : do smth
                    return;
                }
                String chatId = playerObject.getString("chatId");
                ProfileManager.PlayerStatus status = ProfileManager.PlayerStatus.valueOf(playerObject.getString("status"));


                if (!chatId.equals("null")) {
                    BasicActivity currentActivity = ((MyApplication) getApplicationContext()).getCurrentActivity();
                    if (currentActivity == null) return;
                    int chatIdInt = Integer.valueOf(chatId);
                    if (lastChatId == chatIdInt) return;
                    lastChatId = chatIdInt;
                    ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.WAITING);
                    ProfileManager.getPlayer().setChatId(chatIdInt);
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
