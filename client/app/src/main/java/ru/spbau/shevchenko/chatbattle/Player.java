package ru.spbau.shevchenko.chatbattle;

public class Player {
    public Player(int id, String login, int age, Sex sex, int chatId) {
        this.id = id;
        this.login = login;
        this.age = age;
        this.sex = sex;
        this.chatId = chatId;
    }

    public enum Sex {
        MALE, FEMALE;

        public static Sex fromString(String sex) {
            return Sex.valueOf(sex);
        }
    }

    public enum Role {
        PLAYER, LEADER;
    }

    final private int id;
    final private String login;
    final private Sex sex;
    final private int age;

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    private int chatId;

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public Sex getSex() {
        return sex;
    }

    public int getAge() {
        return age;
    }
}
