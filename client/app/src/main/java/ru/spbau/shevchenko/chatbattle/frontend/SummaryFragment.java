package ru.spbau.shevchenko.chatbattle.frontend;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;


public class SummaryFragment extends DialogFragment {
    private boolean playersInitialized = false;
    private TableLayout summaryView;
    private Map<Integer, ChatActivity.Color> playerColors;
    private RelativeLayout summaryParentView;

    private boolean winnerInitialized = false;
    private SummaryPlayer leader;
    private SummaryPlayer winner;
    private Collection<SummaryPlayer> players = new ArrayList<>();

    private void tryFinishInitializing() {
        if (!winnerInitialized || !playersInitialized) {
            return;
        }
        String loginString = getResources().getString(R.string.login);
        String ratingString = getResources().getString(R.string.rating);
        summaryView.addView(createSummaryRow("", new SummaryPlayer(-1, "", loginString, ratingString, ratingString), true, false));
        summaryView.addView(createSummaryRow("Leader:", leader, true));
        summaryView.addView(createSummaryRow("Winner:", winner, false));
        boolean first = true;
        for (SummaryPlayer player : players) {
            if (player.id != winner.id) {
                String comment = "";
                if (first) {
                    comment = "Players:";
                    first = false;
                }
                summaryView.addView(createSummaryRow(comment, player, false));
            }
        }
        summaryView.setEnabled(true);
        final ProgressBar spinner = (ProgressBar) summaryParentView.findViewById(R.id.initializing_progress_bar);
        spinner.setVisibility(View.GONE);

    }

    private View createSummaryRow(String comment, SummaryPlayer player, boolean spanRating) {
        return createSummaryRow(comment, player, spanRating, true);
    }

    private View createSummaryRow(String comment, SummaryPlayer player, boolean spanRating, boolean color) {
        Context context = getActivity();
        TableRow row = new TableRow(context);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        // TODO: fix long nicknames causing rating changes to go out of dialog boundaries
        String[] childStrings = new String[]{comment, player.login,
                spanRating ? player.new_rating : player.old_rating + "->" + player.new_rating};
        for (String childText : childStrings) {
            BasicActivity.addChildTextView(row, childText,
                    getResources().getDimension(R.dimen.summary_font_size), Gravity.CENTER,
                    ContextCompat.getColor(context, R.color.black), null, null);
        }
        if (color) {
            // Set background for login TextView
            row.getChildAt(1).setBackgroundResource(getPlayerColor(player.id).getTextViewId());
        }
        return row;
    }

    public ChatActivity.Color getPlayerColor(int playerId) {
        if (playerColors.containsKey(playerId)) {
            return playerColors.get(playerId);
        }
        ChatActivity.Color color = ChatActivity.Color.values()[playerColors.size()];
        playerColors.put(playerId, color);
        return color;
    }


    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.fragment_summary);
        summaryParentView = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_summary, null);
        Button ok = (Button) summaryParentView.findViewById(R.id.ok_button);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.ok_button) {
                    dismiss();
                }
            }
        });
        summaryView = (TableLayout) summaryParentView.findViewById(R.id.summary_view);
        summaryView.setStretchAllColumns(true);
        builder.setView(summaryParentView);

        summaryView.setEnabled(false);
        //noinspection unchecked
        playerColors = (Map<Integer, ChatActivity.Color>) bundle.getSerializable("player_colors");

        int chatId = bundle.getInt("chat_id", -1);
        RequestMaker.getSummary(chatId, summaryResponseCallback);
        //noinspection unchecked
        Iterable<Integer> playerIds = (Iterable<Integer>) bundle.getSerializable("player_ids");
        String idsString = TextUtils.join(",", playerIds);
        RequestMaker.getRatings(idsString, ratingsResponseCallback);
        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.fragment_summary);
    }

    @SuppressWarnings("WeakerAccess")
    public class SummaryPlayer {
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

    private RequestCallback ratingsResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-OK request result
            JSONArray jsonPlayers;
            try {
                jsonPlayers = new JSONArray(result.getResponse());
                for (int i = 0; i < jsonPlayers.length(); i++) {
                    final JSONObject player = jsonPlayers.getJSONObject(i);
                    String header = i == 0 ? "Players:" : "";
                    players.add(new SummaryPlayer(player.getInt("id"), header, player.getString("login"),
                            player.getString("old_rating"), player.getString("new_rating")));
                }
            } catch (JSONException e) {
                Log.e("ratingsRC", e.getMessage());
            }
            playersInitialized = true;
            tryFinishInitializing();
        }
    };

    private RequestCallback summaryResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-OK request result
            try {
                JSONObject jsonSummary = new JSONObject(result.getResponse());
                JSONObject jsonLeader = jsonSummary.getJSONObject("leader");
                leader = new SummaryPlayer(jsonLeader.getInt("id"),
                        getResources().getString(R.string.leader) + ":",
                        jsonLeader.getString("login"),
                        jsonLeader.getString("old_rating"),
                        jsonLeader.getString("new_rating"));
                JSONObject jsonWinner = jsonSummary.getJSONObject("winner");
                winner = new SummaryPlayer(jsonWinner.getInt("id"),
                        getResources().getString(R.string.winner) + ":",
                        jsonWinner.getString("login"),
                        jsonWinner.getString("old_rating"),
                        jsonWinner.getString("new_rating"));

            } catch (JSONException e) {
                Log.d("leaderboardCallback", result.getResponse());
                Log.e("leaderboardCallback", e.getMessage());
            }
            winnerInitialized = true;
            tryFinishInitializing();
        }
    };
}
