package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;

public class BasicActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final long BATTLE_FOUND_HANDLE_DELAY = 100;
    public static final int NO_MATTER_CODE = 0;

    private static Class<?> lastActivityClass = null;
    private final Handler battleFoundHandler = new Handler();
    private MyApplication myApplication;
    private boolean isVisible = false;

    public enum DrawerAction {

        CHANGE_PASSWORD;

        public enum LeaderAction {

            KICK, MUTE, CHOOSE;

            public int getItemId() {
                switch (this) {
                    case KICK: return R.id.kick_item;
                    case MUTE: return R.id.mute_item;
                    case CHOOSE: return R.id.choose_item;
                }
                throw  new IllegalArgumentException();
            }
        }

        public enum SwitchActivityAction {

            HOME, LEADERBOARD, SPECTATE, LOG_OUT;

            public int getItemId() {
                switch (this) {
                    case HOME: return R.id.home_item;
                    case LEADERBOARD: return R.id.leaderboard_item;
                    case SPECTATE: return R.id.spectate_item;
                    case LOG_OUT: return R.id.log_out_item;
                }
                throw  new IllegalArgumentException();
            }

            public int getPosition() {
                switch (this) {
                    case HOME: return 3;
                    case LEADERBOARD: return 4;
                    case SPECTATE: return 5;
                    case LOG_OUT: return 7;
                }
                throw  new IllegalArgumentException();
            }

            public Class<?> getCorrespondingClass() {
                switch (this) {
                    case HOME: return MenuActivity.class;
                    case LEADERBOARD: return LeaderboardActivity.class;
                    case SPECTATE: return BattleListActivity.class;
                    case LOG_OUT: return LoginActivity.class;
                }
                throw  new IllegalArgumentException();
            }
        }

        public int getItemId() {
            switch (this) {
                case CHANGE_PASSWORD: return R.id.change_password_item;
            }
            throw  new IllegalArgumentException();
        }

    }


    public boolean visible() {
        return isVisible;
    }

    public static Class<?> getLastActivityClass() {
        return lastActivityClass;
    }

    public Handler getBattleFoundHandler() {
        return battleFoundHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApplication = (MyApplication) getApplicationContext();
        myApplication.setCurrentActivity(this);
        isVisible = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        myApplication.setCurrentActivity(this);
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    public void onBackPressed() {
        lastActivityClass = getClass();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        lastActivityClass = getClass();
        clearReferences();
        super.onDestroy();
    }

    public static void addChildTextView(ViewGroup viewGroup, CharSequence childText, Float textSize,
                                        Integer gravity, Integer textColor) {
        final TextView child = new TextView(viewGroup.getContext());
        child.setText(childText);
        if (textSize != null) child.setTextSize(textSize);
        if (gravity != null) child.setGravity(gravity);
        if (textColor != null) child.setTextColor(textColor);
        viewGroup.addView(child);
    }

    private void clearReferences() {
        final BasicActivity currActivity = myApplication.getCurrentActivity();
        if (equals(currActivity))
            myApplication.setCurrentActivity(null);
    }

    public static class BattleFoundRunnable implements Runnable {

        private Player.Role role;
        private FragmentManager fragmentManager;

        public BattleFoundRunnable(Player.Role role, FragmentManager fragmentManager) {
            this.role = role;
            this.fragmentManager = fragmentManager;
        }

        @Override
        public void run() {
            final DialogFragment dialogFragment = new BattleFoundDialogFragment();
            final Bundle bundle = new Bundle();
            bundle.putString("role", role.toString());
            bundle.putInt("chatId", ProfileManager.getPlayer().getChatId());
            dialogFragment.setArguments(bundle);
            dialogFragment.show(fragmentManager, "");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        setResult(resultCode, data);
        finish();
    }

    protected void navigate(int id) {
        for (DrawerAction.SwitchActivityAction action : DrawerAction.SwitchActivityAction.values()) {
            if (action.getItemId() == id) {
                if (action.getCorrespondingClass().isInstance(this)) return;
                Intent intent = new Intent();
                intent.putExtra("goto", action.ordinal());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        if (DrawerAction.CHANGE_PASSWORD.getItemId() == menuItem.getItemId()) {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
            changePasswordDialog.show(getFragmentManager(), "");
        } else if (!specialCheck(menuItem.getItemId())) {
            navigate(menuItem.getItemId());
        }
        menuItem.setChecked(false);
        createDrawer();
        return true;
    }

    protected boolean specialCheck(int id) { return false; }

    protected void createDrawer() {
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().setGroupVisible(R.id.chat_actions_group, false);

        final View headerLayout = navigationView.getHeaderView(0);
        final TextView loginView = (TextView) headerLayout.findViewById(R.id.menu_header_login);
        final TextView ratingView = (TextView) headerLayout.findViewById(R.id.menu_header_rating);
        final TextView timerView = (TextView) headerLayout.findViewById(R.id.timer_view);

        loginView.setText(ProfileManager.getPlayer().getLogin());
        ratingView.setText(String.valueOf(ProfileManager.getPlayer().getRating()));
        timerView.setVisibility(View.INVISIBLE);
    }

}

