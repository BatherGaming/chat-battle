package ru.spbau.shevchenko.chatbattle.frontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class LeaderboardActivity extends AppCompatActivity {
    private TextView leaderboardContent;
    private TextView leaderboardText;

    private RequestCallback leaderboardResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-OK request result
            StringBuilder leaderboardContentText = new StringBuilder();
            try {
                JSONArray jsonLeaderboard = new JSONArray(result.getResponse());
                for (int i = 0; i < jsonLeaderboard.length(); i++) {
                    final JSONObject jsonLeaderboardPlayer = jsonLeaderboard.getJSONObject(i);
                    final Player leaderboardPlayer = Player.fromJSON(jsonLeaderboardPlayer);
                    leaderboardContentText.append(i);
                    leaderboardContentText.append(" ");
                    leaderboardContentText.append(leaderboardPlayer.getLogin());
                    leaderboardContentText.append(" ");
                    leaderboardContentText.append(leaderboardPlayer.getRating());
                    leaderboardContentText.append("\n");
                }
            } catch (JSONException e) {
                Log.e("leaderboardCallback", "json error");
            }
            leaderboardContent.setText(leaderboardContentText.toString());

            leaderboardContent.setEnabled(true);
            leaderboardText.setEnabled(true);
            final ProgressBar spinner = (ProgressBar) findViewById(R.id.initializing_progress_bar);
            spinner.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardText = (TextView) findViewById(R.id.leaderboard_text);
        leaderboardText.setEnabled(false);
        leaderboardContent = (TextView) findViewById(R.id.leaderboard);
        leaderboardContent.setEnabled(false);

        RequestMaker.getLeaderboard(leaderboardResponseCallback);
    }
}
