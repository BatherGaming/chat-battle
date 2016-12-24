package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;

public class MenuActivity extends BasicActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private enum PlayerStatus {
        HELLO, PROFILE, LOG_OUT;
        public String toString() {
            switch (this) {
                case HELLO: return MenuActivity.HELLO;
                case PROFILE: return MenuActivity.PROFILE;
                case LOG_OUT: return MenuActivity.LOG_OUT;
            }
            throw new IllegalArgumentException();
        }
    }

    private static String HELLO;
    private static String LOG_OUT;
    private static String PROFILE;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        HELLO = getString(R.string.hello);
        LOG_OUT = getString(R.string.log_out);
        PROFILE = getString(R.string.profile);

        final Button playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(this);


        final Spinner spinner = (Spinner) findViewById(R.id.menu_spinner);
        final CharSequence[] adaptersItems = {
                PlayerStatus.HELLO.toString() + ProfileManager.getPlayer().getLogin(),
                PlayerStatus.PROFILE.toString(),
                PlayerStatus.LOG_OUT.toString()
        };
        final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                R.layout.menu_activity_spinner_item, adaptersItems);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


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

        }
    }


    @Override
    public void onResume() {
        super.onResume();

        final Spinner spinner = (Spinner) findViewById(R.id.menu_spinner);
        spinner.setSelection(0);

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


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (pos == PlayerStatus.PROFILE.ordinal()) {
            final Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);

        } else if (pos == PlayerStatus.LOG_OUT.ordinal()) {
            finish();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
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


}
