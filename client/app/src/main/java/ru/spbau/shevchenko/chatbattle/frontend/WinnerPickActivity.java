package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class WinnerPickActivity extends BasicActivity implements View.OnClickListener {
    private ProgressBar spinner;
    private Button cancelBtn;
    private Button okBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner_pick);
        final ViewGroup playersLayout = (ViewGroup) findViewById(R.id.players_radio_group);

        cancelBtn = (Button) findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(this);

        okBtn = (Button) findViewById(R.id.ok_button);
        okBtn.setOnClickListener(this);

        spinner = (ProgressBar) findViewById(R.id.choosing_progress_bar);


        final Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        final ArrayList<Integer> playersID = (ArrayList<Integer>) intent.getSerializableExtra("playersId");
        for (int playerId : playersID) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(playerId);
            String x = Integer.toString(playerId);
            radioButton.setText(x);
            playersLayout.addView(radioButton);
        }
    }

    private void setEnabledButtons(boolean enabled){
        okBtn.setEnabled(enabled);
        cancelBtn.setEnabled(enabled);
    }

    private RequestCallback choseCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            switch (result.getStatus()) {
                case OK: {
                    finish();
                    break;
                }
                case FAILED_CONNECTION: {
                    final TextView statusText = (TextView) findViewById(R.id.status_view);
                    spinner.setVisibility(View.GONE);
                    setEnabledButtons(true);
                    statusText.setText(R.string.internet_troubles);
                    break;
                }
                case ERROR: {
                    final TextView statusText = (TextView) findViewById(R.id.status_view);
                    spinner.setVisibility(View.GONE);
                    setEnabledButtons(true);
                    statusText.setText(R.string.unknown_error);
                    break;
                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_button: {
                finish();
                break;
            }
            case R.id.ok_button: {
                int chosen = ((RadioGroup) findViewById(R.id.players_radio_group)).getCheckedRadioButtonId();
                if (chosen == -1) {
                    Toast.makeText(WinnerPickActivity.this, R.string.choice_demand, Toast.LENGTH_LONG).show();
                    break;
                }
                spinner.setVisibility(View.VISIBLE);
                setEnabledButtons(false);
                RequestMaker.chooseWinner(chosen, choseCallback);
            }
        }
    }


}
