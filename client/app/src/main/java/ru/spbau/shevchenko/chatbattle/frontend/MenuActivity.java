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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;


public class MenuActivity extends BasicActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        createDrawer();

        setupPlayButton();


        final ImageButton drawerButton = (ImageButton) findViewById(R.id.menu_drawer_button);
        drawerButton.setOnClickListener(this);

        final TextView chatTextView = (TextView) findViewById(R.id.chat_text_view);
        chatTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/KeyCapsFLF.ttf"));

        final TextView battleTextView = (TextView) findViewById(R.id.battle_text_view);
        battleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/KeyCapsFLF.ttf"));


    }

    private final static int NORMAL_PLAY_BUTTON_SIZE_DP = 180;
    private final static int NORMAL_MARGIN_BOTTOM_DP = 90;
    private final static int PRESSED_PLAY_BUTTON_SIZE_DP = 170;
    private final static int PRESSED_MARGIN_BOTTOM_DP = 95;

    private int toPixels(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void setupPlayButton() {
        final Button playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(this);
        playButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/IMMORTAL.ttf"));

        playButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playButton.getLayoutParams();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        params.width = toPixels(PRESSED_PLAY_BUTTON_SIZE_DP);
                        params.height = toPixels(PRESSED_PLAY_BUTTON_SIZE_DP);
                        params.bottomMargin = toPixels(PRESSED_MARGIN_BOTTOM_DP);
                        playButton.setLayoutParams(params);
                        break;
                    case MotionEvent.ACTION_UP:
                        params.width = toPixels(NORMAL_PLAY_BUTTON_SIZE_DP);
                        params.height = toPixels(NORMAL_PLAY_BUTTON_SIZE_DP);
                        params.bottomMargin = toPixels(NORMAL_MARGIN_BOTTOM_DP);
                        playButton.setLayoutParams(params);
                        break;
                }
                return false;

            }
        });
    }



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
        Class<?> previousClass = BasicActivity.getLastActivityClass();
        if (ProfileManager.getPlayer().getChatId() == -1 && previousClass != null && ChatActivity.class.isAssignableFrom(previousClass)) {
            Log.d("My app", previousClass.getName());
            final Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        ExitFragment exitFragment = new ExitFragment();
        exitFragment.setMenuActivity(this);
        exitFragment.show(getFragmentManager(), "");
    }

    static public class ExitFragment extends DialogFragment {
        public void setMenuActivity(MenuActivity menuActivity) {
            this.menuActivity = menuActivity;
        }

        private MenuActivity menuActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.exit_question)
                    .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final Intent intent = new Intent();
                            intent.putExtra("exit", true);
                            menuActivity.setResult(RESULT_OK, intent);
                            menuActivity.finish();
                        }
                    })
                    .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            return builder.create();
        }
    }

    //



    private DrawerLayout mDrawerLayout;

    void createDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        final View headerLayout = navigationView.getHeaderView(0);
        final TextView loginView = (TextView) headerLayout.findViewById(R.id.menu_header_login);
        final TextView ratingView = (TextView) headerLayout.findViewById(R.id.menu_header_rating);
        navigationView.getMenu().setGroupVisible(R.id.chat_actions_group, false);

        loginView.setText(ProfileManager.getPlayer().getLogin());
        ratingView.setText(String.valueOf(ProfileManager.getPlayer().getRating()));

        TextView timerView = (TextView) headerLayout.findViewById(R.id.timer_view);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        menuItem.setChecked(true);
        navigate(menuItem.getItemId());
        return true;
    }



    static public class ChangePasswordDialog extends DialogFragment {

        private boolean waitingCallback = false;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_password_change, null);
            builder.setView(dialogView)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //LoginDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            final AlertDialog dialog = (AlertDialog) getDialog();
            if (dialog != null) {
                final Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (waitingCallback) return;
                        final EditText oldPassEditText = (EditText) dialog.findViewById(R.id.old_password);
                        String oldPassword = oldPassEditText.getText().toString();
                        final EditText newPassEditText = (EditText) dialog.findViewById(R.id.new_password);
                        String newPassword = newPassEditText.getText().toString();
                        final EditText confirmNewPassEditText = (EditText) dialog.findViewById(R.id.confirm_new_password);
                        String cofirmNewPassword = confirmNewPassEditText.getText().toString();
                        final TextView textView = (TextView) dialog.findViewById(R.id.changing_password_status_view);
                        if (newPassword.equals(cofirmNewPassword)) {
                            setLayoutVisibility(dialog, View.VISIBLE, false);
                            waitingCallback = true;
                            RequestMaker.changePassword(ProfileManager.getPlayer().getId(), oldPassword,
                                    newPassword, new RequestCallback() {
                                        @Override
                                        public void run(RequestResult response) {
                                            waitingCallback = false;
                                            final JSONObject result;
                                            if (response.getStatus() == RequestResult.Status.FAILED_CONNECTION) {
                                                textView.setText(R.string.internet_troubles);
                                                setLayoutVisibility(dialog, View.GONE, true);
                                            } else if (response.getStatus() == RequestResult.Status.ERROR) {
                                                textView.setText(R.string.unknown_error);
                                                setLayoutVisibility(dialog, View.GONE, true);
                                            } else {
                                                try {
                                                    result = new JSONObject(response.getResponse());
                                                    if (result.has("error")) {
                                                        textView.setText(result.getString("error"));
                                                        setLayoutVisibility(dialog, View.GONE, true);
                                                    } else {
                                                        final String ok = getString(R.string.change_success);
                                                        Toast.makeText(dialog.getContext(), ok, Toast.LENGTH_LONG).show();
                                                        dialog.cancel();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            textView.setText(R.string.passwords_dont_match);
                        }
                    }

                });
            }
        }
        public void setLayoutVisibility(AlertDialog dialog, int spinnerVisibility, boolean enableLayout) {
            final EditText oldPassEditText = (EditText) dialog.findViewById(R.id.old_password);
            final EditText newPassEditText = (EditText) dialog.findViewById(R.id.new_password);
            final EditText confirmNewPassEditText = (EditText) dialog.findViewById(R.id.confirm_new_password);
            final TextView textView = (TextView) dialog.findViewById(R.id.changing_password_status_view);
            final ProgressBar spinner = (ProgressBar) dialog.findViewById(R.id.change_password_spinner);

            oldPassEditText.setEnabled(enableLayout);
            newPassEditText.setEnabled(enableLayout);
            confirmNewPassEditText.setEnabled(enableLayout);
            textView.setEnabled(enableLayout);

            spinner.setVisibility(spinnerVisibility);


        }
    }
}
