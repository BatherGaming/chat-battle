package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.SearcherService;

public class MenuActivity extends BasicActivity implements View.OnClickListener/*, AdapterView.OnItemSelectedListener*/ {

    private static boolean isServiceCreated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final Button playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(this);


        /*final Spinner spinner = (Spinner) findViewById(R.id.menu_spinner);
        final CharSequence[] adaptersItems = {
                "Hello, " + ProfileManager.getPlayer().getLogin(),
                "Profile",
                "Log out"
        };
        final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, adaptersItems);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);*/

        if (!isServiceCreated) {
            final Intent intent = new Intent(this, SearcherService.class);
            startService(intent);
            isServiceCreated = true;
        }

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playButton: {
                Log.d("Status=", ProfileManager.getPlayerStatus().toString());
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

    //@Override
    //public void onBackPressed() {
    //}

    /*
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (pos == 1) {
            final Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);

        } else if (pos == 2) {
            finish();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }
    */


}
