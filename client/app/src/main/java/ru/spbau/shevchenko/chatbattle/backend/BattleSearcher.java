package ru.spbau.shevchenko.chatbattle.backend;

import android.util.Log;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import ru.spbau.shevchenko.chatbattle.frontend.SearchActivity;

/**
 * Created by ilya on 11/1/16.
 */

public class BattleSearcher {
    private static Timer timer = null;
    final static private long DELAY = 0;
    final static private long PERIOD = 50;

    public static void findBattle(final SearchActivity searchActivity) {
        final int id = ProfileManager.getPlayer().id;
        RequestMaker.sendRequest(RequestMaker.domainName + "/battlesearch/" + Integer.toString(id), RequestMaker.Method.POST, new RequestCallback() {
            @Override
            public void run(String response) {}
        });
        timer = new Timer();
        timer.schedule(new TimerTask() {
            private boolean pending = false;
            @Override
            public void run() {
                if (pending) return;
                pending = true;
                RequestMaker.sendRequest(RequestMaker.domainName + "/players/" + Integer.toString(id), RequestMaker.Method.GET, new RequestCallback() {
                    @Override
                    public void run(String response) {
                        pending = false;
                        try {
                            JSONObject playerObject = new JSONObject(response);
                            if (playerObject.has("error")) {
                                // TODO : do smth
                                return;
                            }
                            String chatId = playerObject.getString("chatId");
                            if (!chatId.equals("null")) {
                                onServerResponse(Integer.getInteger(chatId), searchActivity);
                            }
                        }
                        catch (Exception e){
                            Log.e("findBattle.timer.run", e.getMessage());
                        }
                    }
                });
            }
        }, DELAY, PERIOD);

    }

    private static void onServerResponse(int chatId, SearchActivity searchActivity) {
        timer.cancel();
        searchActivity.onBattleFound(chatId);
    }

}
