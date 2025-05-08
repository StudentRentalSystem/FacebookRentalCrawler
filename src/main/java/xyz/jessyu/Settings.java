package xyz.jessyu;

import java.io.File;

public final class Settings {
    private static final String FACEBOOK = "https://www.facebook.com/";
    private static final String GROUP_URL = System.getenv("FACEBOOK_GROUP_URL");
    private static final String DB_URL = System.getenv("DB_URL");
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String OS_NAME = System.getProperty("os.name");

    public static String getChromeUserData() {
        String chromeUserData;

        if (OS_NAME.contains("Windows")) {
            // Windows: C:\Users\<User>\fb-crawler
            chromeUserData = USER_HOME + File.separator + "fb-crawler";
        } else if (OS_NAME.contains("Mac")) {
            // macOS: /Users/<User>/fb-crawler
            chromeUserData = USER_HOME + File.separator + "fb-crawler";
        } else if (OS_NAME.contains("Linux")) {
            // Linux: /home/<User>/fb-crawler
            chromeUserData = USER_HOME + File.separator + "fb-crawler";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + OS_NAME);
        }

        return chromeUserData;
    }

    public static String getFacebookUrl() {
        return FACEBOOK;
    }

    public static String getGroupUrl() {
        return GROUP_URL;
    }

    public static String getDbUrl() {
        return DB_URL;
    }
}
