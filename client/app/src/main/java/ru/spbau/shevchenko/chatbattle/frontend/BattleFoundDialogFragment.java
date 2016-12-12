package ru.spbau.shevchenko.chatbattle.frontend;


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

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;

public class BattleFoundDialogFragment extends DialogFragment {
    private int chatId;
    private Player.Role role;

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
                        ((SearchActivity)getActivity()).searchAgain(true, role);
                        getStatusHandler.removeCallbacks(getStatusRunnable);
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getArguments();
        role = Player.Role.valueOf(bundle.getString("role"));
        chatId = bundle.getInt("chatId");
        getStatusHandler.postDelayed(getStatusRunnable, HANDLER_DELAY);

        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                private boolean isClicked = false;
                @Override
                public void onClick(View v) {
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
    }

    final Handler getStatusHandler = new Handler();

    final static private long HANDLER_DELAY = 100;

    final Runnable getStatusRunnable = new Runnable() {
        public void run() {
            RequestMaker.chatStatus(ProfileManager.getPlayer().getId(),
                    chatId, getStatusCallback);
        }
    };

    final RequestCallback getStatusCallback = new RequestCallback() {
        @Override
        public void run(String response) {
            try {
                JSONObject playerObject = new JSONObject(response);
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
                        if (getActivity() == null) return;
                        ((SearchActivity)getActivity()).searchAgain(false, role);
                        getStatusHandler.removeCallbacks(getStatusRunnable);
                        dismiss();
                        break;
                    }
                    default: {
                        Intent intent = role == Player.Role.PLAYER ?
                                new Intent(getActivity(), PlayerActivity.class) :
                                new Intent(getActivity(), LeaderActivity.class);
                        intent.putExtra("chatId", chatId);
                        startActivity(intent);
                    }
                }

            } catch (JSONException e) {
                Log.e("BFDialFragm.run", e.getMessage());
            }
        }
    };

}