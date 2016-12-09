package ru.spbau.shevchenko.chatbattle.backend;

public interface RequestCallback {
    RequestCallback DO_NOTHING = new RequestCallback() {
        @Override
        public void run(String response) {

        }
    };
    void run(String response);
}
