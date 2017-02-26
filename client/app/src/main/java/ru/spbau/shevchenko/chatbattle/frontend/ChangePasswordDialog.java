package ru.spbau.shevchenko.chatbattle.frontend;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ChangePasswordDialog extends DialogFragment {
    private boolean waitingCallback = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        final View dialogView = inflater.inflate(R.layout.dialog_password_change, null);
        builder.setView(dialogView)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
                    final EditText newPassEditText = (EditText) dialog.findViewById(R.id.new_password);
                    final EditText confirmNewPassEditText = (EditText) dialog.findViewById(R.id.confirm_new_password);
                    final TextView textView = (TextView) dialog.findViewById(R.id.changing_password_status_view);

                    final String oldPassword = oldPassEditText.getText().toString();
                    final String newPassword = newPassEditText.getText().toString();
                    final String confirmNewPassword = confirmNewPassEditText.getText().toString();

                    if (newPassword.equals(confirmNewPassword)) {
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

    private void setLayoutVisibility(AlertDialog dialog, int spinnerVisibility, boolean enableLayout) {
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
