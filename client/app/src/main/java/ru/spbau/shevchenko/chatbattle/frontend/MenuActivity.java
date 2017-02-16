package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;


public class MenuActivity extends BasicActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final int NORMAL_PLAY_BUTTON_SIZE_DP = 180;
    private static final int NORMAL_MARGIN_BOTTOM_DP = 90;
    private static final int PRESSED_PLAY_BUTTON_SIZE_DP = 170;
    private static final int PRESSED_MARGIN_BOTTOM_DP = 95;

    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        createDrawer();
        setupPlayButton();

        final ImageButton drawerButton = (ImageButton) findViewById(R.id.menu_drawer_button);
        drawerButton.setOnClickListener(this);

        final TextView chatTextView = (TextView) findViewById(R.id.chat_text_view);
        chatTextView.setTypeface(Typeface.createFromAsset(getAssets(),
                getResources().getString(R.string.KeyCapsFont)));

        final TextView battleTextView = (TextView) findViewById(R.id.battle_text_view);
        battleTextView.setTypeface(Typeface.createFromAsset(getAssets(),
                getResources().getString(R.string.KeyCapsFont)));
    }

    public void setupPlayButton() {
        final Button playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(this);
        playButton.setTypeface(Typeface.createFromAsset(getAssets(),
                getResources().getString(R.string.ImmortalFont)));
        playButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playButton.getLayoutParams();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        params.width = toPixels(PRESSED_PLAY_BUTTON_SIZE_DP);
                        params.height = toPixels(PRESSED_PLAY_BUTTON_SIZE_DP);
                        params.bottomMargin = toPixels(PRESSED_MARGIN_BOTTOM_DP);
                        break;
                    case MotionEvent.ACTION_UP:
                        params.width = toPixels(NORMAL_PLAY_BUTTON_SIZE_DP);
                        params.height = toPixels(NORMAL_PLAY_BUTTON_SIZE_DP);
                        params.bottomMargin = toPixels(NORMAL_MARGIN_BOTTOM_DP);
                        break;
                }
                playButton.setLayoutParams(params);
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button: {
                switch (ProfileManager.getPlayerStatus()) {
                    case IN_QUEUE_AS_LEADER:
                    case IN_QUEUE_AS_PLAYER:
                    case IDLE: {
                        final Intent intent = new Intent(this, SearchActivity.class);
                        startActivity(intent);
                        break;
                    }
                    default: {
                        startActivity(new Intent(this, ChatActivity.class));
                    }
                }
                break;
            }
            case R.id.menu_drawer_button: {
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final Button play_button = (Button) findViewById(R.id.play_button);
        play_button.clearAnimation();
        Log.e("menu", "onResume");
        // call it again because of new rating
        createDrawer();
        // if we are back from chat, we should show search screen
        Class<?> previousClass = BasicActivity.getLastActivityClass();
        if (ProfileManager.getPlayer().getChatId() == -1 &&
                previousClass != null && ChatActivity.class.isAssignableFrom(previousClass)) {
            final Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        ExitFragment exitFragment = new ExitFragment();
        exitFragment.show(getFragmentManager(), "");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        menuItem.setChecked(true);
        navigate(menuItem.getItemId());
        return true;
    }

    private void createDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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

    private void navigate(int id) {
        switch (id) {
            case R.id.menu_change_password: {
                ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
                changePasswordDialog.show(getFragmentManager(), "");
                break;
            }
            case R.id.menu_leaderboard: {
                final Intent intent = new Intent(this, LeaderboardActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menu_battle_list: {
                final Intent intent = new Intent(this, BattleListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menu_log_out: {
                finish();
                break;
            }
        }
    }

    private int toPixels(int dp) {
        //noinspection NumericCastThatLosesPrecision
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public static class ExitFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.exit_question)
                    .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final Intent intent = new Intent();
                            intent.putExtra("exit", true);
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            return builder.create();
        }
    }

}
