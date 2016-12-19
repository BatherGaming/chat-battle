package ru.spbau.shevchenko.chatbattle.frontend;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.BattleSearcher;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

import static ru.spbau.shevchenko.chatbattle.Player.*;

public class BattleFoundDialogFragment extends DialogFragment {
    private int chatId;
    private Role role;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.battle_found)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RequestMaker.decline(ProfileManager.getPlayer().getId());
                        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);
                        Activity activity = getActivity();
                        if (activity instanceof SearchActivity) {
                            ((SearchActivity) getActivity()).searchAgain(true, role);
                        }
                        getStatusHandler.removeCallbacks(getStatusRunnable);
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getArguments();
        role = Role.valueOf(bundle.getString("role"));
        chatId = bundle.getInt("chatId");
        getStatusHandler.postDelayed(getStatusRunnable, HANDLER_DELAY);

        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                private boolean isClicked = false;

                @Override
                public void onClick(View v) {
                    hasAccepted = true;
                    if (isClicked) return;
                    RequestMaker.accept(ProfileManager.getPlayer().getId());
                    isClicked = true;
                }
            });
            d.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    return true;
                }
            });
        }
        startTime = System.currentTimeMillis();
    }

    private long startTime;
    private boolean hasAccepted;
    final Handler getStatusHandler = new Handler();

    final static private long HANDLER_DELAY = 100;
    final private static int MAX_IDLENESS_TIME = 5000;

    final Runnable getStatusRunnable = new Runnable() {
        public void run() {
            RequestMaker.chatStatus(ProfileManager.getPlayer().getId(),
                    chatId, getStatusCallback);
        }
    };

    final RequestCallback getStatusCallback = new RequestCallback() {
        @Override
        public void run(RequestResult requestResult) {
            try {
                JSONObject playerObject = new JSONObject(requestResult.getResponse());
                if (playerObject.has("error")) {
                    return;
                }
                String result = playerObject.getString("result");
                switch (result) {
                    case ("waiting"): {
                        getStatusHandler.postDelayed(getStatusRunnable, HANDLER_DELAY);
                        break;
                    }
                    case ("won't start"): {
                        Activity currentActivity = getActivity();
                        if (currentActivity == null) return;
                        if (hasAccepted || System.currentTimeMillis() - startTime < MAX_IDLENESS_TIME) {
                            if (currentActivity instanceof SearchActivity) {
                                ((SearchActivity) currentActivity).searchAgain(false, role);
                            }
                            BattleSearcher.findBattle(role);
                            switch (role) {
                                case PLAYER: {
                                    ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IN_QUEUE_AS_PLAYER);
                                    break;
                                }
                                case LEADER: {
                                    ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IN_QUEUE_AS_LEADER);
                                    break;
                                }
                            }
                            getStatusHandler.removeCallbacks(getStatusRunnable);
                        } else {
                            ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);
                            if (currentActivity instanceof SearchActivity) {
                                ((SearchActivity) currentActivity).searchAgain(true, role);
                            }
                        }
                        dismiss();
                        break;
                    }
                    default: {

                        switch (role) {
                            case PLAYER: {
                                ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.CHATTING_AS_PLAYER);
                                break;
                            }
                            case LEADER: {
                                ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.CHATTING_AS_LEADER);
                                break;
                            }
                        }
                        Intent intent = role == Role.PLAYER ?
                                new Intent(getActivity(), PlayerActivity.class) :
                                new Intent(getActivity(), LeaderActivity.class);
                        intent.putExtra("chatId", chatId);
                        startActivity(intent);
                        dismiss();
                        break;
                    }
                }

            } catch (JSONException e) {
                Log.e("BFDialFragm.run", e.getMessage());
            }
        }
    };

}