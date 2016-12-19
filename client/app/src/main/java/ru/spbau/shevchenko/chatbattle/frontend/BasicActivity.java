package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;

public class BasicActivity extends AppCompatActivity {
    final public static long BATTLE_FOUND_HANDLE_DELAY = 100;

    private MyApplication myApplication;
    private boolean isVisible = false;
    private static Class<?> lastActivityClass = null;
    final private Handler battleFoundHandler = new Handler();


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


}

