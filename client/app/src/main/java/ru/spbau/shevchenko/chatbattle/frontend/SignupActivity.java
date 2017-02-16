package ru.spbau.shevchenko.chatbattle.frontend;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.Player;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class SignupActivity extends BasicActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        final Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(this);

        final TextView chatTextView = (TextView) findViewById(R.id.chat_text_view);
        chatTextView.setTypeface(Typeface.createFromAsset(getAssets(),
                getResources().getString(R.string.KeyCapsFont)));

        final TextView battleTextView = (TextView) findViewById(R.id.battle_text_view);
        battleTextView.setTypeface(Typeface.createFromAsset(getAssets(),
                getResources().getString(R.string.KeyCapsFont)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signup_button: {
                // Getting all the data from form
                final String login = ((TextView) findViewById(R.id.login_edit)).getText().toString();
                final String password = ((TextView) findViewById(R.id.password_edit)).getText().toString();
                final String passwordConfirm = ((TextView) findViewById(R.id.password_confirm_edit)).getText().toString();
                final String email = ((TextView) findViewById(R.id.email_edit)).getText().toString();

                // Check password equality
                if (!password.equals(passwordConfirm)) {
                    ((TextView) findViewById(R.id.status_view)).setText(R.string.password_dont_match);
                    return;
                }
                // Set status
                ((TextView) findViewById(R.id.status_view)).setText(R.string.signin_up);
                // Sign up
                ProfileManager.signUp(new Player(0, login, -1, -1), password, email, this);
                break;
            }

        }
    }

    public void completeSignup() {
        ((TextView) findViewById(R.id.status_view)).setText("");
        ((TextView) findViewById(R.id.login_edit)).setText("");
        ((TextView) findViewById(R.id.password_edit)).setText("");
        ((TextView) findViewById(R.id.password_confirm_edit)).setText("");
        finish();
    }

    public void failedSignup(CharSequence reason) {
        final TextView statusView = (TextView) findViewById(R.id.status_view);
        statusView.setText(reason);
    }

    public void signupResponse(RequestResult.Status status) {
        switch (status) {
            case OK: {
                completeSignup();
            }
            case FAILED_CONNECTION: {
                failedSignup(getResources().getString(R.string.internet_troubles));
            }
            case ERROR: {
                failedSignup(getResources().getString(R.string.unknown_error));
            }
        }
    }

}
