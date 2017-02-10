package ru.spbau.shevchenko.chatbattle.frontend;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Base64;
import android.util.Log;
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
import java.util.Locale;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;


public class LeaderboardAdapter extends BaseAdapter {
    private ArrayList<Player> leaderboard = new ArrayList<>();


    private int playerPosition;
    private final Context context;

    LeaderboardAdapter(Context context){
        this.context = context;
    }

    public void setLeaderboard(ArrayList<Player> leaderboard) {
        this.leaderboard = leaderboard;
        notifyDataSetChanged();
    }

    public void setPlayerPosition(int playerPosition) {
        this.playerPosition = playerPosition;
    }

    @Override
    public int getCount() {
        return leaderboard.size();
    }

    @Override
    public Object getItem(int i) {
        return leaderboard.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LeaderViewHolder holder;
        if (convertView == null) {
            LayoutInflater leaderInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = leaderInflater.inflate(R.layout.leader, null);
            holder = new LeaderViewHolder((TextView) convertView.findViewById(R.id.position),
                    (TextView) convertView.findViewById(R.id.login),
                    (TextView) convertView.findViewById(R.id.rating));
            convertView.setTag(holder);
        } else {
            holder = (LeaderViewHolder) convertView.getTag();
        }
        final Player player = leaderboard.get(position);
        holder.ratingView.setText(String.valueOf(player.getRating()));
        holder.loginView.setText(String.valueOf(player.getLogin()));
        if (player.getId() == ProfileManager.getPlayer().getId()) {
            holder.positionView.setText(String.valueOf(playerPosition));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                convertView.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.player_background, null));
            }
        }
        else {
            holder.positionView.setText(String.valueOf(position+1));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                convertView.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.leader_background, null));
            }
        }
        return convertView;
    }

    private static class LeaderViewHolder {
        final private TextView positionView;
        final private TextView loginView;
        final private TextView ratingView;

        LeaderViewHolder(TextView positionView, TextView loginView, TextView ratingView) {
            this.positionView = positionView;
            this.loginView = loginView;
            this.ratingView = ratingView;
        }
    }
}
