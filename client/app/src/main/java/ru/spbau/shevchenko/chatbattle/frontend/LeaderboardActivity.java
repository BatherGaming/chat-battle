package ru.spbau.shevchenko.chatbattle.frontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

            for (Player player : leaderboard) {
                final TableRow row = (TableRow) LayoutInflater.from(LeaderboardActivity.this).inflate(R.layout.leader, null);
                TextView tv = (TextView) row.getChildAt(0);
                tv.setText(String.valueOf(player.getId()));
                tv = (TextView) row.getChildAt(1);
                tv.setText(String.valueOf(player.getLogin()));
                tv = (TextView) row.getChildAt(2);
                tv.setText(String.valueOf(player.getRating()));
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
