package ru.spbau.shevchenko.chatbattle.frontend;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Locale;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;

public class ProfileActivity extends BasicActivity implements View.OnClickListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Display profile info
        final Player player = ProfileManager.getPlayer();

        final TextView loginView = (TextView) findViewById(R.id.login_value_view);
        loginView.setText(player.getLogin());
        final TextView ageView = (TextView) findViewById(R.id.age_value_view);
        ageView.setText(String.format(Locale.getDefault(), "%d", player.getAge()));
        final TextView sexView = (TextView) findViewById(R.id.sex_value_view);
        sexView.setText(player.getSex().toString());
        final TextView ratingView = (TextView) findViewById(R.id.rating_value_view);
        ratingView.setText(String.format(Locale.getDefault(), "%d", player.getRating()));

        final Button button = (Button) findViewById(R.id.change_password_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_password_button: {
                ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
                changePasswordDialog.show(getFragmentManager(), "");
                break;
            }
        }
    }

    static public class ChangePasswordDialog extends DialogFragment {
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
                        final EditText oldPassEditText = (EditText) dialog.findViewById(R.id.old_password);
                        String oldPassword = oldPassEditText.getText().toString();
                        final EditText newPassEditText = (EditText) dialog.findViewById(R.id.new_password);
                        String newPassword = newPassEditText.getText().toString();
                        final EditText confirmNewPassEditText = (EditText) dialog.findViewById(R.id.confirm_new_password);
                        String cofirmNewPassword = confirmNewPassEditText.getText().toString();
                        if (newPassword.equals(cofirmNewPassword)) {
                            RequestMaker.changePassword(ProfileManager.getPlayer().getId(), oldPassword,
                                    newPassword, new RequestCallback() {
                                        @Override
                                        public void run(String response) {
                                            final JSONObject result;
                                            try {
                                                result = new JSONObject(response);
                                                if (result.has("error")) {
                                                    final TextView textView = (TextView) dialog.findViewById(R.id.changing_password_status_view);
                                                    textView.setText(result.getString("error"));
                                                } else {
                                                    final String ok = getString(R.string.change_success);
                                                    Toast.makeText(dialog.getContext(), ok, Toast.LENGTH_LONG).show();
                                                    dialog.cancel();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        } else {
                            final TextView textView = (TextView) dialog.findViewById(R.id.changing_password_status_view);
                            textView.setText(R.string.passwords_dont_match);
                        }
                    }
                });
            }
        }
    }
}
