package ru.spbau.shevchenko.chatbattle;

public class Message {
    public Message(String text, int authorId, int chatId) {
        this.text = text;
        this.authorId = authorId;
        this.chatId = chatId;
    }

    final private String text;
    final private int authorId;
    final private int chatId;

    public String getText() {
        return text;
    }

    public int getAuthorId() {
        return authorId;
    }

    public int getChatId() {
        return chatId;
    }
}
