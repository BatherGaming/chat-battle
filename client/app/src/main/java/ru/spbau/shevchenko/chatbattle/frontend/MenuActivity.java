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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;
import ru.spbau.shevchenko.chatbattle.backend.StringConstants;


public class MenuActivity extends BasicActivity implements View.OnClickListener {

    private enum PlayerStatus {
        NAME, RATING, CHANGE_PASSWORD, LEADERBOARD, LOG_OUT;
        public String toString() {
            switch (this) {
                case NAME: return StringConstants.getNAME();
                case RATING: return StringConstants.getRATING();
                case CHANGE_PASSWORD: return StringConstants.getCHANGE_PASSWORD();
                case LEADERBOARD: return StringConstants.getLEADERBOARD();
                case LOG_OUT: return StringConstants.getLOG_OUT();
            }
            throw new IllegalArgumentException();
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        onNavigationDrawerCreate();


        final ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
        playButton.setOnClickListener(this);

        final ImageButton drawerButton = (ImageButton) findViewById(R.id.menu_drawer_button);
        drawerButton.setOnClickListener(this);

        final TextView chatTextView = (TextView) findViewById(R.id.chat_text_view);
        chatTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/KeyCapsFLF.ttf"));

        final TextView battleTextView = (TextView) findViewById(R.id.battle_text_view);
        battleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/KeyCapsFLF.ttf"));




    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playButton: {
                switch (ProfileManager.getPlayerStatus()) {
                    case IN_QUEUE_AS_LEADER:
                    case IN_QUEUE_AS_PLAYER:
                    case IDLE: {
                        final Intent intent = new Intent(this, SearchActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case CHATTING_AS_LEADER: {
                        final Intent intent = new Intent(this, LeaderActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case CHATTING_AS_PLAYER: {
                        final Intent intent = new Intent(this, PlayerActivity.class);
                        startActivity(intent);
                    }
                }
                break;
            }
            case R.id.menu_drawer_button: {
                openDrawer();
            }

        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // call it again because of new rating
        onNavigationDrawerCreate();

        Class<?> previousClass = BasicActivity.getLastActivityClass();
        if (ProfileManager.getPlayer().getChatId() == -1 && previousClass != null && AbstractChat.class.isAssignableFrom(previousClass)) {
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


    public void onNavigationDrawerCreate() {
        //final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        final CharSequence[] adaptersItems = {
                PlayerStatus.NAME.toString() + ": " + ProfileManager.getPlayer().getLogin(),
                PlayerStatus.RATING.toString() + ": " + ProfileManager.getPlayer().getRating(),
                PlayerStatus.CHANGE_PASSWORD.toString(),
                PlayerStatus.LEADERBOARD.toString(),
                PlayerStatus.LOG_OUT.toString()
        };
        final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                R.layout.drawer_list_item, adaptersItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    public void openDrawer(){
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    private void selectItem(int position) {
        if (position == PlayerStatus.CHANGE_PASSWORD.ordinal()) {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
            changePasswordDialog.show(getFragmentManager(), "");
        } else if (position == PlayerStatus.LEADERBOARD.ordinal()) {
            final Intent intent = new Intent(this, LeaderboardActivity.class);
            startActivity(intent);
        } else if (position == PlayerStatus.LOG_OUT.ordinal()) {
            finish();
        }
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
