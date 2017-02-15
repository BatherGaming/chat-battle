package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class BattleListActivity extends BasicActivity {
    private TableLayout battleListView;
    private ProgressBar spinner;

    private class Chat {
        final public int id;
        final public int leaderId;
        final public String leader;
        final public ArrayList<String> players;
        final public int averageRating;

        private Chat(int id, int leaderId, String leader, ArrayList<String> players, int averageRating) {
            this.id = id;
            this.leaderId = leaderId;
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
                    final Chat chat = new Chat(jsonChat.getInt("chat_id"), jsonChat.getInt("leader_id"),
                                         jsonChat.getString("leader"),
                                         playerLogins,
                                         jsonChat.getInt("sum_rating") / playerLogins.size());
                    chats.add(chat);
                }
            } catch (JSONException e) {
                Log.e("chatsRC", e.getMessage());
            }
            LayoutInflater layoutInflater = LayoutInflater.from(BattleListActivity.this);
            for (final Chat chat : chats) {
                final TableRow row = new TableRow(BattleListActivity.this);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent(BattleListActivity.this, ChatActivity.class);
                        intent.putExtra("leader_id", chat.leaderId);
                        ProfileManager.getPlayer().setChatId(chat.id);
                        startActivity(intent);
                    }
                });
                row.setBackgroundColor(ContextCompat.getColor(BattleListActivity.this, R.color.red));
                int blackColor = ContextCompat.getColor(BattleListActivity.this, R.color.black);
                BasicActivity.addChildTextView(row, chat.leader, null, Gravity.CENTER, blackColor, null, null);
                BasicActivity.addChildTextView(row, chat.players.toString(), null, Gravity.CENTER, blackColor, null, null);
                BasicActivity.addChildTextView(row, String.valueOf(chat.averageRating), null, Gravity.CENTER, blackColor, null, null);
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
