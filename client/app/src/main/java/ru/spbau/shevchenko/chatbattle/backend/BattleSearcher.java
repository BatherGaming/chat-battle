package ru.spbau.shevchenko.chatbattle.backend;

import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.frontend.SearchActivity;

public class BattleSearcher {
    final static private long HANDLER_DELAY = 500;

    private static Handler handler;
    private static Runnable checkBattle;

    public static void findBattle(final SearchActivity searchActivity) {
        final int id = ProfileManager.getPlayer().id;
        RequestMaker.sendRequest(RequestMaker.domainName + "/battlemaker/" + Integer.toString(id), RequestMaker.Method.POST, new RequestCallback() {
            @Override
            public void run(String response) {}
        });
        handler = new Handler();
        checkBattle = new Runnable() {
            @Override
            public void run() {
                RequestMaker.sendRequest(RequestMaker.domainName + "/players/" + Integer.toString(id), RequestMaker.Method.GET, new RequestCallback() {
                    @Override
                    public void run(String response) {
                        try {
                            JSONObject playerObject = new JSONObject(response);
                            if (playerObject.has("error")) {
                                // TODO : do smth
                                return;
                            }
                            String chatId = playerObject.getString("chatId");
                            if (!chatId.equals("null")) {
                                searchActivity.onBattleFound(Integer.valueOf(chatId));
//                                onServerResponse(Integer.getInteger(chatId), searchActivity);
                            } else {
                                handler.postDelayed(checkBattle, HANDLER_DELAY);
                            }
                        } catch (JSONException e) {
                            Log.e("findBattle.handler.run", e.getMessage());
                        }
                    }
                });
            }
        };
        handler.postDelayed(checkBattle, HANDLER_DELAY);


    }

    private static void onServerResponse(int chatId, SearchActivity searchActivity) {
        handler.removeCallbacks(checkBattle);
        searchActivity.onBattleFound(chatId);
    }

}
