package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;


public class MenuActivity extends BasicActivity implements View.OnClickListener {

    private static final int NORMAL_PLAY_BUTTON_SIZE_DP = 180;
    private static final int NORMAL_MARGIN_BOTTOM_DP = 90;
    private static final int PRESSED_PLAY_BUTTON_SIZE_DP = 170;
    private static final int PRESSED_MARGIN_BOTTOM_DP = 95;


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
                        startActivityForResult(intent, NO_MATTER_CODE);
                        break;
                    }
                    default: {
                        startActivityForResult(new Intent(this, ChatActivity.class), NO_MATTER_CODE);
                    }
                }
                break;
            }
            case R.id.menu_drawer_button: {
                final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            startActivityForResult(intent, NO_MATTER_CODE);
        }
    }

    @Override
    public void onBackPressed() {
        ExitFragment exitFragment = new ExitFragment();
        exitFragment.show(getFragmentManager(), "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        int ord = data.getIntExtra("goto", -1);
        goTo(ord);
    }

    @Override
    protected void navigate(int id) {
        for (DrawerAction.SwitchActivityAction action : DrawerAction.SwitchActivityAction.values()) {
            if (action.getItemId() == id) {
                goTo(action.ordinal());
                return;
            }
        }
    }

    private void goTo(int ord) {
        if (ord == DrawerAction.SwitchActivityAction.LEADERBOARD.ordinal()) {
            final Intent intent = new Intent(this, LeaderboardActivity.class);
            startActivityForResult(intent, ord);
        } else if (ord == DrawerAction.SwitchActivityAction.SPECTATE.ordinal()) {
            final Intent intent = new Intent(this, BattleListActivity.class);
            startActivityForResult(intent, ord);
        } else if (ord == DrawerAction.SwitchActivityAction.LOG_OUT.ordinal()) {
            finish();
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
