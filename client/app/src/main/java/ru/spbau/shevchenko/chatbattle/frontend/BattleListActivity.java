package ru.spbau.shevchenko.chatbattle.frontend;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class BattleListActivity extends BasicActivity {
    private TableLayout battleListView;
    private ProgressBar spinner;

    private class Chat {
        final public String leader;
        final public ArrayList<String> players;
        final public int averageRating;

        private Chat(String leader, ArrayList<String> players, int averageRating) {
            this.leader = leader;
            this.players = players;
            this.averageRating = averageRating;
        }
    }
    private RequestCallback chatsResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-ok results
            ArrayList<Chat> chats = new ArrayList<>();
            try {
                JSONArray jsonChats = new JSONArray(result.getResponse());
                chats = new ArrayList<>();
                for (int i = 0; i < jsonChats.length(); i++) {
                    JSONObject jsonChat = jsonChats.getJSONObject(i);
                    JSONArray jsonPlayers = jsonChat.getJSONArray("players");
                    ArrayList<String> playerLogins = new ArrayList<>();
                    for (int j = 0; j < jsonPlayers.length(); j++) {
                        playerLogins.add(jsonPlayers.getString(j));
                    }
                    final Chat chat = new Chat(jsonChat.getString("leader"),
                                         playerLogins,
                                         jsonChat.getInt("sum_rating") / playerLogins.size());
                    chats.add(chat);
                }
            } catch (JSONException e) {
                Log.e("chatsRC", e.getMessage());
            }
            LayoutInflater layoutInflater = LayoutInflater.from(BattleListActivity.this);
            for (Chat chat : chats) {
                final TableRow row = (TableRow) layoutInflater.inflate(R.layout.battle_list_row, null);
                row.setBackgroundColor(ContextCompat.getColor(BattleListActivity.this, R.color.red));
                int blackColor = ContextCompat.getColor(BattleListActivity.this, R.color.black);
                BasicActivity.addChildTextView(row, chat.leader, null, Gravity.CENTER, blackColor);
                BasicActivity.addChildTextView(row, chat.players.toString(), null, Gravity.CENTER, blackColor);
                BasicActivity.addChildTextView(row, String.valueOf(chat.averageRating), null, Gravity.CENTER, blackColor);
                battleListView.addView(row);
            }
            spinner.setVisibility(View.GONE);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_list);
        battleListView = (TableLayout) findViewById(R.id.battle_list_view);
        battleListView.setStretchAllColumns(true);
        spinner = (ProgressBar) findViewById(R.id.initializing_progress_bar);
        RequestMaker.getChats(chatsResponseCallback);
    }
}
