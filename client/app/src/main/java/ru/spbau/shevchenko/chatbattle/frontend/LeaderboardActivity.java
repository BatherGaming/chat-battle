package ru.spbau.shevchenko.chatbattle.frontend;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
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
    private TableLayout leaderboardView;
    private TextView leaderboardText;

    private RequestCallback leaderboardResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-OK request result
            final StringBuilder leaderboardContentText = new StringBuilder();
            final ArrayList<Player> leaderboard = new ArrayList<Player>();
            try {
                JSONArray jsonLeaderboard = new JSONArray(result.getResponse());
                int count = Math.min(10, jsonLeaderboard.length());
                boolean currentAdded = false;
                for (int i = 0; i < count; i++) {
                    final JSONObject jsonLeaderboardPlayer = jsonLeaderboard.getJSONObject(i);
                    final Player leaderboardPlayer = Player.fromJSON(jsonLeaderboardPlayer);
                    leaderboard.add(leaderboardPlayer);
                    if (leaderboardPlayer.getId() == ProfileManager.getPlayer().getId()){
                        currentAdded = true;
                    }
                }
                if (!currentAdded) {
                    leaderboard.add(ProfileManager.getPlayer());
                }
            } catch (JSONException e) {
                Log.d("leaderboardCallback", result.getResponse());
                Log.e("leaderboardCallback", "json error");
            }

            int position = 0;
            for (Player player : leaderboard) {
                position++;
                final TableRow row = new TableRow(LeaderboardActivity.this);
                if (ProfileManager.getPlayer().getId() == player.getId()) {
                    row.setBackgroundResource(R.drawable.leader_background_player);
                }
                else {
                    row.setBackgroundResource(R.drawable.leader_background);
                }
                final String[] childTexts = new String[]{String.valueOf(position),
                        String.valueOf(player.getLogin()), String.valueOf(player.getRating())};
                for (String text : childTexts) {
                    BasicActivity.addChildTextView(row, text, 20f, null, ContextCompat.getColor(LeaderboardActivity.this, R.color.black));
                }
                leaderboardView.addView(row);
            }

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
        leaderboardView = (TableLayout) findViewById(R.id.leaderboard_view);
        leaderboardView.setStretchAllColumns(true);
        leaderboardView.setEnabled(false);


        RequestMaker.getLeaderboard(leaderboardResponseCallback);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
