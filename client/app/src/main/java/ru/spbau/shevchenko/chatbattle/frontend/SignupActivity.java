package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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

        Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signup_button: {
                // Getting all the data from form
                String login = ((EditText) findViewById(R.id.login_edit)).getText().toString();
                int age = Integer.valueOf(((EditText) findViewById(R.id.age_edit)).getText()
                        .toString());
                RadioGroup rg = (RadioGroup) findViewById(R.id.sex_radio_group);
                Player.Sex sex = (rg.getCheckedRadioButtonId() == R.id.male_radio_button ? Player.Sex.MALE : Player.Sex.FEMALE);
                String password = ((EditText) findViewById(R.id.password_edit)).getText().toString();
                String passwordConfirm = ((EditText) findViewById(R.id.password_confirm_edit)).getText().toString();

                // Check password equality
                if (!password.equals(passwordConfirm)) {
                    ((TextView) findViewById(R.id.status_view)).setText(R.string.password_dont_match);
                    return;
                }
                // Set status
                ((TextView) findViewById(R.id.status_view)).setText(R.string.signin_up);
                // Sign up
                ProfileManager.signUp(new Player(0, login, age, sex, -1, -1), password, this);
                break;
            }

        }
    }

    public void completeSignup() {
        ((TextView) findViewById(R.id.status_view)).setText("");
        ((EditText) findViewById(R.id.login_edit)).setText("");
        ((EditText) findViewById(R.id.age_edit)).setText("");
        ((EditText) findViewById(R.id.password_edit)).setText("");
        ((EditText) findViewById(R.id.password_confirm_edit)).setText("");
        ((RadioGroup) findViewById(R.id.sex_radio_group)).clearCheck();


        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    public void failedSignup(String reason) {
        TextView statusView = (TextView) findViewById(R.id.status_view);
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
