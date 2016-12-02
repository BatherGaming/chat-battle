package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;

public class BasicActivity extends AppCompatActivity {
    protected MyApplication myApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApplication = (MyApplication) this.getApplicationContext();
    }

    protected void onResume() {
        super.onResume();
        myApplication.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences() {
        BasicActivity currActivity = myApplication.getCurrentActivity();
        if (this.equals(currActivity))
            myApplication.setCurrentActivity(null);
    }

    final private Handler battleFoundHandler = new Handler();

    public Handler getBattleFoundHandler() {
        return battleFoundHandler;
    }

    final public static long BATTLE_FOUND_HANDLE_DELAY = 100;

    public static class battleFoundRunnable implements Runnable {
        private Player.Role role;
        private FragmentManager fragmentManager;

        public battleFoundRunnable(Player.Role role, FragmentManager fragmentManager) {
            this.role = role;
            this.fragmentManager = fragmentManager;
        }

        @Override
        public void run() {
            DialogFragment dialogFragment = new BattleFoundDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString("role", role.toString());
            dialogFragment.setArguments(bundle);
            dialogFragment.show(fragmentManager, "");
        }
    }

    ;
}

