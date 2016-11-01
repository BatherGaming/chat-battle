package ru.spbau.shevchenko.chatbattle.backend;


import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.frontend.ChatActivity;

/**
 * Created by Nikolay on 28.10.16.
 */

public class Chatter {
    public static void sendMessage(ChatActivity activity, String message) {
        activity.update(new Message("Cocker", message));
    }
}
