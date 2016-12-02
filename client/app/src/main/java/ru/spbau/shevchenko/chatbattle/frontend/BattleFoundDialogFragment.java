package ru.spbau.shevchenko.chatbattle.frontend;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;

public class BattleFoundDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.battle_found)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Bundle bundle = getArguments();
                        Player.Role role = Player.Role.valueOf(bundle.getString("role"));
                        int chatId = bundle.getInt("chatId");

                        Intent intent = role == Player.Role.PLAYER ?
                                new Intent(getActivity(), PlayerActivity.class) :
                                new Intent(getActivity(), LeaderActivity.class);
                        intent.putExtra("chatId", chatId);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        return builder.create();
    }
}