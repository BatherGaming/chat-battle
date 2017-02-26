package ru.spbau.shevchenko.chatbattle.frontend;

import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

import static java.lang.StrictMath.min;

public class BattleListActivity extends BasicActivity {
    private List<Chat> chats = new ArrayList<>();
    private static final int MAX_BATTLES = 10;
    private int cntBattles;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_list);
        createDrawer();

        final TextView chatTextView = (TextView) findViewById(R.id.chat_text_view);
        chatTextView.setTypeface(Typeface.createFromAsset(getAssets(), getResources().getString(R.string.KeyCapsFont)));

        final TextView battleTextView = (TextView) findViewById(R.id.battle_text_view);
        battleTextView.setTypeface(Typeface.createFromAsset(getAssets(), getResources().getString(R.string.KeyCapsFont)));

        final TextView topBattles = (TextView) findViewById(R.id.battle_list_text);
        topBattles.setTypeface(Typeface.createFromAsset(getAssets(), getResources().getString(R.string.ImmortalFont)));

        Button updateBtn = (Button) findViewById(R.id.update);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });
        final ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();
            }
        });

        update();

    }

    private void update() {
        setVisibilities(View.VISIBLE, View.GONE, View.GONE, View.GONE);
        chats.clear();
        RequestMaker.getChats(chatsResponseCallback);
    }

    private void setVisibilities(int spinnerVis, int pagerVis, int updateVis, int noVis) {
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.initializing_progress_bar);
        final Button updateBtn = (Button) findViewById(R.id.update);
        final ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        final TextView noChats = (TextView) findViewById(R.id.no_chat_view);

        spinner.setVisibility(spinnerVis);
        updateBtn.setVisibility(updateVis);
        mPager.setVisibility(pagerVis);
        noChats.setVisibility(noVis);
    }


    @SuppressWarnings("WeakerAccess")
    public class Chat implements Serializable {

        public final int id;
        private final int leaderId;

        private final String leader;
        private final ArrayList<String> players;
        private final int averageRating;

        private Chat(int id, int leaderId, String leader, ArrayList<String> players, int averageRating) {
            this.id = id;
            this.leaderId = leaderId;
            this.leader = leader;
            this.players = players;
            this.averageRating = averageRating;
        }

        public int getId() {
            return id;
        }

        public int getLeaderId() {
            return leaderId;
        }

        public String getLeader() {
            return leader;
        }

        public ArrayList<String> getPlayers() {
            return players;
        }

        public int getAverageRating() {
            return averageRating;
        }

    }


    private RequestCallback chatsResponseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            // TODO: handle non-ok results
            try {
                JSONArray jsonChats = new JSONArray(result.getResponse());
                for (int i = 0; i < jsonChats.length(); i++) {
                    JSONObject jsonChat = jsonChats.getJSONObject(i);
                    JSONArray jsonPlayers = jsonChat.getJSONArray("players");
                    ArrayList<String> playerLogins = new ArrayList<>();
                    for (int j = 0; j < jsonPlayers.length(); j++) {
                        playerLogins.add(jsonPlayers.getString(j));
                    }
                    final Chat chat = new Chat(jsonChat.getInt("chat_id"),
                            jsonChat.getInt("leader_id"),
                            jsonChat.getString("leader"),
                            playerLogins,
                            jsonChat.getInt("sum_rating") / playerLogins.size());
                    chats.add(chat);
                }
            } catch (JSONException e) {
                Log.e("chatsRC", e.getMessage());
            }
            cntBattles = min(MAX_BATTLES, chats.size());
            if (cntBattles != 0) {
                PagerAdapter mPagerAdapter = new BattleListFragmentAdapter(getSupportFragmentManager());
                final ViewPager mPager = (ViewPager) findViewById(R.id.pager);
                mPager.setAdapter(mPagerAdapter);
                setVisibilities(View.GONE, View.VISIBLE, View.VISIBLE, View.GONE);
            } else {
                setVisibilities(View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
            }
        }
    };


    private class BattleListFragmentAdapter extends FragmentStatePagerAdapter {

        private BattleListFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return BattleListFragment.create(position, chats.get(position));
        }

        @Override
        public int getCount() {
            return cntBattles;
        }
    }


    private class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) {
                view.setAlpha(0);

            } else if (position <= 0) {
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                view.setAlpha(1 - position);
                view.setTranslationX(pageWidth * -position);
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else {
                view.setAlpha(0);
            }
        }
    }

}

