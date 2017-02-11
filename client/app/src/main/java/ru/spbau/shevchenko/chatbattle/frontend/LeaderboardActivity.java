package ru.spbau.shevchenko.chatbattle.frontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class LeaderboardActivity extends BasicActivity {
    private ListView leaderboardView;
    private TextView leaderboardText;
    private LeaderboardAdapter leaderboardAdapter;

    private RequestCallback leaderboardResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-OK request result
            final StringBuilder leaderboardContentText = new StringBuilder();
            final ArrayList<Player> leaderboard = new ArrayList<Player>();
            try {
                JSONArray jsonLeaderboard = new JSONArray(result.getResponse());
                int count = Math.min(10, jsonLeaderboard.length());
                for (int i = 0; i < jsonLeaderboard.length(); i++) {
                    final JSONObject jsonLeaderboardPlayer = jsonLeaderboard.getJSONObject(i);
                    final Player leaderboardPlayer = Player.fromJSON(jsonLeaderboardPlayer);
                    if (leaderboardPlayer.getId() == ProfileManager.getPlayer().getId()){
                        leaderboardAdapter.setPlayerPosition(i+1);
                        leaderboard.add(leaderboardPlayer);
                    }
                    else if (i < count) {
                        leaderboard.add(leaderboardPlayer);
                    }
                }
            } catch (JSONException e) {
                Log.d("leaderboardCallback", result.getResponse());
                Log.e("leaderboardCallback", "json error");
            }
            leaderboardAdapter.setLeaderboard(leaderboard);
            leaderboardView.setEnabled(true);
            leaderboardText.setEnabled(true);
            final ProgressBar spinner = (ProgressBar) findViewById(R.id.initializing_progress_bar);
            spinner.setVisibility(View.GONE);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardText = (TextView) findViewById(R.id.leaderboard_text);
        leaderboardText.setEnabled(false);
        leaderboardView = (ListView) findViewById(R.id.leaderboard_view);
        leaderboardView.setEnabled(false);
        leaderboardAdapter = new LeaderboardAdapter(this);
        leaderboardView.setAdapter(leaderboardAdapter);

        RequestMaker.getLeaderboard(leaderboardResponseCallback);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
