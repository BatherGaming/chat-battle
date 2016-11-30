package ru.spbau.shevchenko.chatbattle.backend;

import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.frontend.SearchActivity;

public class BattleSearcher {
    final static private long HANDLER_DELAY = 500;

    private static Handler handler;
    private static Runnable checkBattle;


    public static void findBattle(final SearchActivity searchActivity, final Player.Role role) {
        final int id = ProfileManager.getPlayer().getId();

        RequestMaker.findBattle(role, id);

        handler = new Handler();
        checkBattle = new Runnable() {
            @Override
            public void run() {
                RequestMaker.checkIfFound(id, new checkIfFoundCallback(searchActivity, role));
            }
        };
        handler.postDelayed(checkBattle, HANDLER_DELAY);
    }

    private static class checkIfFoundCallback implements RequestCallback {

        private SearchActivity searchActivity;
        private Player.Role role;

        checkIfFoundCallback(SearchActivity searchActivity, Player.Role role) {
            this.searchActivity = searchActivity;
            this.role = role;
        }

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
                    int chatIdInt = Integer.valueOf(chatId);
                    searchActivity.onBattleFound(chatIdInt, role);
                } else {
                    handler.postDelayed(checkBattle, HANDLER_DELAY);
                }
            } catch (JSONException e) {
                Log.e("findBattle.handler.run", e.getMessage());
            }
        }
    }


}
