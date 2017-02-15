package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.content.ContextCompat;
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
import java.util.HashMap;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;


public class SummaryFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private boolean playersInitialized = false;
    private TableLayout summaryView;
    private HashMap<Integer, ChatActivity.Color> playerColors;
    private RelativeLayout summaryParentView;

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
        String loginString = getResources().getString(R.string.login);
        String ratingString = getResources().getString(R.string.rating);
        Context context = getActivity();
//        final TableRow titleRow = new TableRow(context);
//        TextView titleView = (TextView) LayoutInflater.from(context).inflate(R.layout.spanned_text_view_3, null);
//        titleView.setText(getResources().getString(R.string.summary));
//        titleView.setTextSize(getResources().getDimension(R.dimen.summary_title_font_size));
//        titleView.setGravity(Gravity.CENTER);
//        titleView.setTextColor(ContextCompat.getColor(context, R.color.black));
//        titleRow.addView(titleView);
//        summaryView.addView(titleRow);
        summaryView.addView(createSummaryRow("", new SummaryPlayer(-1, "", loginString, ratingString, ratingString), true, false));
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
        String[] childStrings = new String[] {comment, player.login,
                spanRating ? player.new_rating : player.old_rating + "->" + player.new_rating};
        for (String childText : childStrings) {
            BasicActivity.addChildTextView(row, childText,
                    getResources().getDimension(R.dimen.summary_font_size), Gravity.CENTER,
                    ContextCompat.getColor(context, R.color.black));
        }
        if (color) {
            // Set background for login TextView
            row.getChildAt(1).setBackgroundResource(getPlayerColor(player.id).getTextViewId());
        }
        return row;
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
            try {
                JSONObject jsonSummary = new JSONObject(result.getResponse());
                JSONObject jsonLeader = jsonSummary.getJSONObject("leaderboard_row");
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

    @Override
    public void onStart() {
        super.onStart();
        //getDialog().getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.cyan));

    }

    public ChatActivity.Color getPlayerColor(int playerId) {
        if (playerColors.containsKey(playerId)) {
            Log.d("getPC", "" + playerId);
            Log.d("getPC", "" + playerColors.get(playerId));
            return playerColors.get(playerId);
        }
        ChatActivity.Color color = ChatActivity.Color.values()[playerColors.size()];
        playerColors.put(playerId, color);
        Log.d("getPC", "" + color);
        return color;
    }


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
        playerColors = (HashMap<Integer, ChatActivity.Color>)bundle.getSerializable("player_colors");

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.fragment_summary);
    }
}
