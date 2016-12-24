package ru.spbau.shevchenko.chatbattle.backend;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.frontend.BasicActivity;
import ru.spbau.shevchenko.chatbattle.frontend.LoginActivity;

public class SearcherRunnable implements Runnable {
    static private final int SLEEPING_TIME = 200; // mills
    private MyApplication myApplication;
    private boolean waitingCallback = false;
    private int lastChatId = -1;
    private boolean needDialog = false;


    public SearcherRunnable(MyApplication myApplication) {
        this.myApplication = myApplication;
    }

    @Override
    public void run() {
        while (true) {
            if (!waitingCallback && ProfileManager.getPlayer() != null) {
                RequestMaker.checkIfFound(ProfileManager.getPlayer().getId(), checkIfFoundCallback);
                waitingCallback = true;
            }
            try {
                Thread.sleep(SLEEPING_TIME);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private BasicActivity getActivity() {
        BasicActivity result = null;
        while (result == null) result = myApplication.getCurrentActivity();
        return result;
    }

    private RequestCallback checkIfFoundCallback = new RequestCallback() {

        @Override
        public void run(String response) {
            try {
                waitingCallback = false;
                if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.CHATTING_AS_LEADER)
                    return;
                if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.CHATTING_AS_PLAYER)
                    return;
                if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.WAITING)
                    return;

                final JSONObject playerObject = new JSONObject(response);
                if (playerObject.has("error")) {
                    // TODO : do smth
                    return;
                }
                final int playerId = playerObject.getInt("id");
                if (playerId != ProfileManager.getPlayer().getId()) return;
                final String chatId = playerObject.getString("chatId");
                ProfileManager.PlayerStatus status = ProfileManager.PlayerStatus.valueOf(playerObject.getString("status"));


                if (!chatId.equals("null")) {
                    int chatIdInt = Integer.valueOf(chatId);
                    final BasicActivity currentActivity = getActivity();
                    if (currentActivity.getClass().equals(LoginActivity.class)) return;
                    if (lastChatId == chatIdInt) {
                        tryShowDialog(currentActivity, chatIdInt, status);
                        return;
                    }
                    lastChatId = chatIdInt;
                    needDialog = true;
                    removeNotifications();
                    notifyUser();
                    tryShowDialog(currentActivity, chatIdInt, status);
                } else {
                    removeNotifications();
                }
            } catch (JSONException e) {
                Log.e("chIfFoCallb.run", e.getMessage());
            }
        }
    };

    private void tryShowDialog(BasicActivity currentActivity, int chatIdInt, ProfileManager.PlayerStatus status) {
        if (needDialog && currentActivity.visible()) {
            needDialog = false;
            showDialog(currentActivity, chatIdInt, status);
        }
    }

    private void showDialog(BasicActivity currentActivity, int chatIdInt, ProfileManager.PlayerStatus status) {
        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.WAITING);
        ProfileManager.getPlayer().setChatId(chatIdInt);
        currentActivity.getBattleFoundHandler().postDelayed(
                new BasicActivity.BattleFoundRunnable(
                        status == ProfileManager.PlayerStatus.CHATTING_AS_LEADER ? Player.Role.LEADER : Player.Role.PLAYER,
                        currentActivity.getFragmentManager()),
                BasicActivity.BATTLE_FOUND_HANDLE_DELAY);
    }

    private static final int PRIORITY_HIGH = 5;

    private void notifyUser() {
        final BasicActivity curActivity = getActivity();
        final Intent notificationIntent = new Intent(curActivity, curActivity.getClass());
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent intent = PendingIntent.getActivity(curActivity, 0, notificationIntent, 0);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(curActivity)
                .setSmallIcon(R.drawable.send)
                .setContentTitle(curActivity.getString(R.string.app_name))
                .setContentIntent(intent)
                .setPriority(PRIORITY_HIGH)
                .setContentText("Battle has been found!")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        final NotificationManager mNotificationManager = (NotificationManager) curActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void removeNotifications() {
        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }
}
