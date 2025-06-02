package xyz.jessyu;

import io.github.studentrentalsystem.LLMClient;

import java.io.File;

public final class Settings {
    private static final String FACEBOOK = "https://www.facebook.com/";
    private static final String GROUP_URL = System.getenv("FACEBOOK_GROUP_URL");
    private static final String DB_URL = System.getenv("DB_URL");
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String OS_NAME = System.getProperty("os.name");
    private static final String DB_NAME = "app";
    private static final String DB_COLLECTION = "house_rental";
    private static final LLMClient.ModelType LLM_MODEL_TYPE = LLMClient.ModelType.LLAMA3_8B;
    private static final int RETRY_ATTEMPTS = 1;

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

    public static String getDbName() {
        return DB_NAME;
    }

    public static String getDbCollection() {
        return DB_COLLECTION;
    }

    public static LLMClient.ModelType getLlmModelType() {
        return LLM_MODEL_TYPE;
    }

    public static int getRetryAttempts() { return RETRY_ATTEMPTS; }
}
