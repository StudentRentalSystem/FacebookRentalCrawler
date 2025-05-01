package xyz.jessyu;

import java.io.File;

public final class Settings {
    private static final String FACEBOOK = "https://www.facebook.com/";
    private static final String GROUP_URL = System.getenv("FACEBOOK_GROUP_URL");
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String OS_NAME = System.getProperty("os.name");

    public static String getChromeUserData() {
        String chromeUserData;

        if (OS_NAME.contains("Windows")) {
            // Windows: C:\Users\<User>\AppData\Local\Google\Chrome\User Data
            chromeUserData = USER_HOME + File.separator + "AppData" + File.separator +
                    "Local" + File.separator + "Google" + File.separator + "Chrome" + File.separator + "User Data";
        } else if (OS_NAME.contains("Mac")) {
            // macOS: /Users/<User>/Library/Application Support/Google/Chrome
            chromeUserData = USER_HOME + File.separator + "Library" + File.separator +
                    "Application Support" + File.separator + "Google" + File.separator + "Chrome";
        } else if (OS_NAME.contains("Linux")) {
            // Linux: /home/<User>/.config/google-chrome
            chromeUserData = USER_HOME + File.separator + ".config" + File.separator + "google-chrome";
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
}
