package ru.spbau.shevchenko.chatbattle.backend;


import java.util.ArrayList;
import java.util.List;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.frontend.ChatActivity;


public class Chatter {
    public static void sendMessage(ChatActivity activity, String message) {
        activity.update(new Message("Me", message));
    }

    private static List<Message> messageList = new ArrayList<>();
    public static List<Message> getMessages() {
        messageList.add(new Message("Hey", "Arnold"));
        return messageList;
    }
}
