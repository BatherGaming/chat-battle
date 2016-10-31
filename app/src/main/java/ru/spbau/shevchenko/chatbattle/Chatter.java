package ru.spbau.shevchenko.chatbattle;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikolay on 28.10.16.
 */

public class Chatter {
    public static void sendMessage(ChatActivity activity, String message) {
        activity.update(new Message("Cocker", message));
    }
}
