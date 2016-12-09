package ru.spbau.shevchenko.chatbattle;

public class Message {
    // TODO: add fromJSON

    public Message(int id, String text, int authorId, int chatId, String tag) {
        this.id = id;
        this.text = text;
        this.authorId = authorId;
        this.chatId = chatId;
        this.tag = tag;
    }

    final private int id;
    final private String text;
    final private int authorId;
    final private int chatId;
    final private String tag;

    public int getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

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
