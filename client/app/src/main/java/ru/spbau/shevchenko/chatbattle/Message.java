package ru.spbau.shevchenko.chatbattle;

public class Message {
    public String text;
    public int authorId;
    public int chatId;
    public Message(String text, int authorId, int chatId){
        this.text = text;
        this.authorId = authorId;
        this.chatId = chatId;
    }
}
