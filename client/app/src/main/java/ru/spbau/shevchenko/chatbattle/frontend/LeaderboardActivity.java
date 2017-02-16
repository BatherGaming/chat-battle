package ru.spbau.shevchenko.chatbattle.frontend;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class LeaderboardActivity extends BasicActivity {
    private static final int TOP_COUNT = 30;
    private TableLayout leaderboardView;
    private TextView leaderboardText;

    public enum RowElement {
        POSITION, LOGIN, RATING
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        final TextView chatTextView = (TextView) findViewById(R.id.chat_text_view);
        chatTextView.setTypeface(Typeface.createFromAsset(getAssets(), getResources().getString(R.string.KeyCapsFont)));

        final TextView battleTextView = (TextView) findViewById(R.id.battle_text_view);
        battleTextView.setTypeface(Typeface.createFromAsset(getAssets(), getResources().getString(R.string.KeyCapsFont)));


        leaderboardText = (TextView) findViewById(R.id.leaderboard_text);
        leaderboardText.setEnabled(false);
        leaderboardView = (TableLayout) findViewById(R.id.leaderboard_view);
        leaderboardView.setColumnStretchable(RowElement.LOGIN.ordinal(), true);
        leaderboardView.setEnabled(false);
        leaderboardText.setTypeface(Typeface.createFromAsset(getAssets(), getResources().getString(R.string.ImmortalFont)));


        RequestMaker.getLeaderboard(leaderboardResponseCallback);
    }

    private RequestCallback leaderboardResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-OK request result
            final Collection<Player> leaderboard = new ArrayList<>();
            try {
                JSONArray jsonLeaderboard = new JSONArray(result.getResponse());
                int count = Math.min(TOP_COUNT, jsonLeaderboard.length());
                boolean currentAdded = false;
                for (int i = 0; i < count; i++) {
                    final JSONObject jsonLeaderboardPlayer = jsonLeaderboard.getJSONObject(i);
                    final Player leaderboardPlayer = Player.fromJSON(jsonLeaderboardPlayer);
                    leaderboard.add(leaderboardPlayer);
                    if (leaderboardPlayer.getId() == ProfileManager.getPlayer().getId()) {
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
                @SuppressLint("InflateParams")
                final TableRow row = (TableRow) LayoutInflater.from(LeaderboardActivity.this)
                        .inflate(R.layout.leaderboard_row, null);

                ((TextView) row.getChildAt(RowElement.POSITION.ordinal()))
                        .setText(String.valueOf(position));
                final SpannableStringBuilder s = new SpannableStringBuilder(player.getLogin());
                // Span to make text bold
                final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
                s.setSpan(bss, 0, s.length(), 0);
                ((TextView) row.getChildAt(RowElement.LOGIN.ordinal())).setText(s);
                if (ProfileManager.getPlayer().getId() == player.getId()) {
                    row.getChildAt(RowElement.LOGIN.ordinal())
                            .setBackgroundResource(R.drawable.table_row_this_player);
                }
                ((TextView) row.getChildAt(RowElement.RATING.ordinal()))
                        .setText(String.valueOf(player.getRating()));

                leaderboardView.addView(row);
            }

            leaderboardView.setEnabled(true);
            leaderboardText.setEnabled(true);
            final ProgressBar spinner = (ProgressBar) findViewById(R.id.initializing_progress_bar);
            spinner.setVisibility(View.GONE);
        }
    };
}
