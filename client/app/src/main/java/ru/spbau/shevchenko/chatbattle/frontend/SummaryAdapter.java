package ru.spbau.shevchenko.chatbattle.frontend;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;


public class SummaryAdapter extends BaseAdapter {

    private ArrayList<SummaryActivity.SummaryPlayer> players = new ArrayList<>();



    private int playerPosition;
    private final Context context;
    private HashMap<Integer, AbstractChat.Color> playerColors;

    SummaryAdapter(Context context){
        this.context = context;
    }

    public void setPlayers(ArrayList<SummaryActivity.SummaryPlayer> players) {
        this.players = players;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return players.size();
    }

    @Override
    public Object getItem(int i) {
        return players.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SummaryViewHolder holder;
        if (convertView == null) {
            LayoutInflater leaderInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = leaderInflater.inflate(R.layout.summary_player, null);
            holder = new SummaryViewHolder((TextView) convertView.findViewById(R.id.header),
                    (TextView) convertView.findViewById(R.id.login),
                    (TextView) convertView.findViewById(R.id.old_rating),
                    (TextView) convertView.findViewById(R.id.new_rating));
            convertView.setTag(holder);
        } else {
            holder = (SummaryViewHolder) convertView.getTag();
        }
        final SummaryActivity.SummaryPlayer player = players.get(position);
        holder.headerView.setText(player.header);
        holder.loginView.setText(player.login);
        holder.loginView.setBackgroundResource(getPlayerColor(player.id).getTextViewId());
        holder.oldRatingView.setText(player.old_rating);
        holder.newRatingView.setText(player.new_rating);
        return convertView;
    }

    public void setPlayerColors(HashMap<Integer, AbstractChat.Color> playerColors) {
        this.playerColors = playerColors;
    }

    public AbstractChat.Color getPlayerColor(int playerId) {
        if (playerColors.containsKey(playerId)) {
            Log.d("getPC", "" + playerId);
            Log.d("getPC", "" + playerColors.get(playerId));
            return playerColors.get(playerId);
        }
        AbstractChat.Color color = AbstractChat.Color.values()[playerColors.size()];
        playerColors.put(playerId, color);
        Log.d("getPC", "" + color);
        return color;
    }

    private static class SummaryViewHolder {
        final private TextView headerView;
        final private TextView loginView;
        final private TextView oldRatingView;
        final private TextView newRatingView;

        private SummaryViewHolder(TextView headerView, TextView loginView, TextView oldRatingView, TextView newRatingView) {
            this.headerView = headerView;
            this.loginView = loginView;
            this.oldRatingView = oldRatingView;
            this.newRatingView = newRatingView;
        }
    }
}
