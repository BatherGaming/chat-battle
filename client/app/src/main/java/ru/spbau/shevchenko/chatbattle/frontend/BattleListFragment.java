package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;

public class BattleListFragment extends Fragment {

    private int mPageNumber;
    private BattleListActivity.Chat chat;

    public static BattleListFragment create(int pageNumber, BattleListActivity.Chat chat) {
        BattleListFragment fragment = new BattleListFragment();
        fragment.mPageNumber = pageNumber;
        fragment.chat = chat;
        return fragment;
    }

    public BattleListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_battle_list, container, false);

        final TextView battlePos = (TextView) rootView.findViewById(R.id.battle_position);
        final TextView leaderNote = (TextView) rootView.findViewById(R.id.leader_note);
        final TextView leader = (TextView) rootView.findViewById(R.id.leader_login);
        final TextView playersNote = (TextView) rootView.findViewById(R.id.players_note);
        final TextView players = (TextView) rootView.findViewById(R.id.players_logins);
        final TextView averNote = (TextView) rootView.findViewById(R.id.average_rating_note);
        final TextView averRating = (TextView) rootView.findViewById(R.id.average_rating);

        setUnderlinedText(battlePos, getResources().getString(R.string.battle2) + " #" + String.valueOf(1 + mPageNumber));
        setUnderlinedText(leaderNote, getResources().getString(R.string.leader) + ":");
        setUnderlinedText(playersNote, getResources().getString(R.string.players) + ":");
        setUnderlinedText(averNote, getResources().getString(R.string.average_rating) + ":");

        leader.setText(chat.getLeader());
        players.setText(TextUtils.join("\n", chat.getPlayers()));
        averRating.setText(String.valueOf(chat.getAverageRating()));

        rootView.findViewById(R.id.spectate_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent((BattleListActivity) getActivity(), ChatActivity.class);
                intent.putExtra("leader_id", chat.getLeaderId());
                ProfileManager.getPlayer().setChatId(chat.id);
                startActivityForResult(intent, BasicActivity.NO_MATTER_CODE);
            }
        });

        return rootView;
    }

    private void setUnderlinedText(TextView textView, String s) {
        Spannable str = new SpannableString(s);
        str.setSpan(new UnderlineSpan(), 0, s.length(), 0);
        textView.setText(str);
    }

    public int getPageNumber() {
        return mPageNumber;
    }
}
