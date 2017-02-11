package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class SummaryActivity extends AppCompatActivity {
    public class SummaryPlayer{
        final public int id;
        final public String header;
        final public String login;
        final public String old_rating;
        final public String new_rating;

        public SummaryPlayer(int id, String header, String login, String old_rating, String new_rating) {
            this.id = id;
            this.header = header;
            this.login = login;
            this.old_rating = old_rating;
            this.new_rating = new_rating;
        }
    }
    private ListView summaryView;
    private SummaryAdapter summaryAdapter;

    private RequestCallback summaryResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-OK request result
            final ArrayList<Player> leaderboard = new ArrayList<>();
            try {
                JSONObject jsonSummary = new JSONObject(result.getResponse());
                ArrayList<SummaryPlayer> players = new ArrayList<>();
                JSONObject leader = jsonSummary.getJSONObject("leader");
                players.add(new SummaryPlayer(leader.getInt("id"), "Leader:", leader.getString("login"),
                        leader.getString("old_rating"), ""));
                JSONObject winner = jsonSummary.getJSONObject("winner");
                players.add(new SummaryPlayer(winner.getInt("id"), "Winner:", winner.getString("login"),
                        winner.getString("old_rating"), winner.getString("new_rating")));
                JSONArray jsonPlayers = jsonSummary.getJSONArray("players");
                for (int i = 0; i < jsonPlayers.length(); i++) {
                    final JSONObject player = jsonPlayers.getJSONObject(i);
                    String header = i == 0 ? "Players:" : "";
                    players.add(new SummaryPlayer(player.getInt("id"), header, player.getString("login"),
                            player.getString("old_rating"), player.getString("new_rating")));
                }
                summaryAdapter.setPlayers(players);
            } catch (JSONException e) {
                Log.d("leaderboardCallback", result.getResponse());
                Log.e("leaderboardCallback", e.getMessage());
            }
            summaryView.setEnabled(true);
            final ProgressBar spinner = (ProgressBar) findViewById(R.id.initializing_progress_bar);
            spinner.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        final Intent intent = getIntent();

        summaryView = (ListView) findViewById(R.id.summary_view);
        summaryView.setEnabled(false);
        summaryAdapter = new SummaryAdapter(this);
        HashMap<Integer, Chat.Color> playerColors = (HashMap<Integer, Chat.Color>)intent.getSerializableExtra("player_colors");
        summaryAdapter.setPlayerColors(playerColors);
        summaryView.setAdapter(summaryAdapter);

        int chatId = intent.getIntExtra("chat_id", -1);
        Log.d("SummAct", String.valueOf(chatId));

        RequestMaker.getSummary(chatId, summaryResponseCallback);
    }
}
