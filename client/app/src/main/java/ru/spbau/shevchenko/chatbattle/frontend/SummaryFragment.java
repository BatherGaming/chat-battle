package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class SummaryFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private boolean playersInitialized = false;
    private TableLayout summaryView;
    private HashMap<Integer, Chat.Color> playerColors;
    private View parentView;

    private boolean winnerInitialized = false;
    private SummaryPlayer leader;
    private SummaryPlayer winner;

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @SuppressWarnings("WeakerAccess")
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
    private ArrayList<SummaryPlayer> winnerLeader = new ArrayList<>();
    private ArrayList<SummaryPlayer> players = new ArrayList<>();

    void tryFinishInitializing(){
        if (!winnerInitialized || !playersInitialized) {
            return;
        }

        summaryView.addView(createSummaryRow("Leader:", leader, true));
        summaryView.addView(createSummaryRow("Winner:", winner, false));
        boolean first = true;
        for (SummaryPlayer player : players){
            if (player.id != winner.id) {
                String comment = "";
                if (first)
                    comment = "Players:";
                summaryView.addView(createSummaryRow(comment, player, false));
            }
        }
        summaryView.setEnabled(true);
        //final ProgressBar spinner = (ProgressBar) parentView.findViewById(R.id.initializing_progress_bar);
        //spinner.setVisibility(View.GONE);

    }

    private View createSummaryRow(String comment, SummaryPlayer player, boolean spanRating) {
        Context context = getActivity();
        TableRow row = new TableRow(context);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        for (int i = 0; i < 4; i++ ) {
            row.addView(new TextView(context));
        }
        setText(row, 0, comment);
        setText(row, 1, player.login);
        row.getChildAt(1).setBackgroundResource(getPlayerColor(player.id).getTextViewId());
        setText(row, 2, player.new_rating);
        setText(row, 3, player.old_rating);
        return row;
    }


    private void setText(TableRow view, int index, String text) {
        ((TextView) view.getChildAt(index)).setText(text);
    }

    private RequestCallback ratingsResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-OK request result
            JSONArray jsonPlayers = null;
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
            final ArrayList<Player> leaderboard = new ArrayList<>();
            try {
                JSONObject jsonSummary = new JSONObject(result.getResponse());
                JSONObject jsonLeader = jsonSummary.getJSONObject("leader");
                leader = new SummaryPlayer(jsonLeader.getInt("id"), "Leader:", jsonLeader.getString("login"),
                        jsonLeader.getString("old_rating"), "");
                JSONObject jsonWinner = jsonSummary.getJSONObject("winner");
                winner = new SummaryPlayer(jsonWinner.getInt("id"), "Winner:", jsonWinner.getString("login"),
                        jsonWinner.getString("old_rating"), jsonWinner.getString("new_rating"));

            } catch (JSONException e) {
                Log.d("leaderboardCallback", result.getResponse());
                Log.e("leaderboardCallback", e.getMessage());
            }
            winnerInitialized = true;
            tryFinishInitializing();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        getDialog()
                .getWindow()
                .setLayout(500,
                        300);

    }

    public Chat.Color getPlayerColor(int playerId) {
        if (playerColors.containsKey(playerId)) {
            Log.d("getPC", "" + playerId);
            Log.d("getPC", "" + playerColors.get(playerId));
            return playerColors.get(playerId);
        }
        Chat.Color color = Chat.Color.values()[playerColors.size()];
        playerColors.put(playerId, color);
        Log.d("getPC", "" + color);
        return color;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        summaryView = (TableLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_summary, null);
      //  summaryView = (TableLayout) parentView.findViewById(R.id.summary_view);
        builder.setCancelable(false)
               .setPositiveButton("heh", this)
               .setView(summaryView);

        summaryView.setEnabled(false);
        playerColors = (HashMap<Integer, Chat.Color>)bundle.getSerializable("player_colors");

        int chatId = bundle.getInt("chat_id", -1);
        RequestMaker.getSummary(chatId, summaryResponseCallback);
        StringBuilder idsString = new StringBuilder();
        ArrayList<Integer> playerIds = (ArrayList<Integer>) bundle.getSerializable("player_ids");
        for (int i = 0; i < playerIds.size(); i++) {
            if (i!=0) {
                idsString.append(',');
            }
            idsString.append(playerIds.get(i));
        }
        RequestMaker.getRatings(idsString.toString(), ratingsResponseCallback);
        return builder.create();
    }


}
