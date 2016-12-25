package ru.spbau.shevchenko.chatbattle.backend;


public final class StringConstants {
    private static String HELLO;
    private static String LOG_OUT;
    private static String PROFILE;
    private static String MALE;
    private static String FEMALE;

    public static String getHELLO() {
        return HELLO;
    }
    public static void setHELLO(String HELLO) {
        StringConstants.HELLO = HELLO;
    }

    public static String getLOG_OUT() {
        return LOG_OUT;
    }
    public static void setLOG_OUT(String logOut) {
        LOG_OUT = logOut;
    }

    public static String getPROFILE() {
        return PROFILE;
    }
    public static void setPROFILE(String PROFILE) {
        StringConstants.PROFILE = PROFILE;
    }

    public static String getMALE() {
        return MALE;
    }
    public static void setMALE(String MALE) {
        StringConstants.MALE = MALE;
    }

    public static String getFEMALE() {
        return FEMALE;
    }
    public static void setFEMALE(String FEMALE) {
        StringConstants.FEMALE = FEMALE;
    }

}
