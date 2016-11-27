package ru.spbau.shevchenko.chatbattle;

/**
 * Created by ilya on 11/1/16.
 */

public class Player {
    public Player(int id, String login, int age, Sex sex, Integer chatId) {
        this.id = id;
        this.login = login;
        this.age = age;
        this.sex = sex;
        this.chatId = chatId;
    }

    public enum Sex{
        MALE, FEMALE;
        public String toString(){
            switch (this){
                case MALE: return "Male";
                case FEMALE: return "Female";
            }
            return "Queer";
        }

        public static Sex fromString(String sex) {
            return (sex.toLowerCase().equals("male") ? MALE : FEMALE);
        }
    }

    public enum Role {
        PLAYER, LEADER;
        public String toString() {
            switch (this) {
                case PLAYER: return "player";
                case LEADER: return "leader";
            }
            return ""; // TODO: fill this
        }
    }

    public int id;
    public String login;
    public Sex sex;
    public int age;
    public Integer chatId;
}
