package xyz.jessyu;

import io.github.studentrentalsystem.LLMClient;
import io.github.studentrentalsystem.LLMConfig;

import java.io.File;

public final class Settings {
    private static final String promptPath = "extract_prompt.txt";
    private static final String rentalPostsPath = "rental_posts.json";
    private static final String extractedDataPath = "extracted_data.json";
    private static final String outputPath = "src/main/resources/" + extractedDataPath;

    private static final String FACEBOOK = "https://www.facebook.com/";
    private static final String GROUP_URL = System.getenv("FACEBOOK_GROUP_URL");
    private static final String DB_URL = System.getenv("DB_URL");
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String OS_NAME = System.getProperty("os.name");
    private static final String DB_NAME = "app";
    private static final String DB_COLLECTION = "house_rental";
    private static final String LLM_MODEL_TYPE = "llama3:8b";
    private static final int RETRY_ATTEMPTS = 1;
    private static final LLMConfig llmConfig = new LLMConfig(
            LLMConfig.LLMMode.CHAT,
            System.getenv("LLM_SERVER_ADDRESS"),
            Integer.parseInt(System.getenv("LLM_SERVER_PORT")),
            LLM_MODEL_TYPE,
            false,
            null
    );

    public static String getPromptPath() {
        return promptPath;
    }

    public static String getRentalPostsPath() {
        return rentalPostsPath;
    }

    public static String getExtractedDataPath() {
        return extractedDataPath;
    }

    public static String getOutputPath() {
        return outputPath;
    }

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

    public static String getLlmModelType() {
        return LLM_MODEL_TYPE;
    }

    public static int getRetryAttempts() { return RETRY_ATTEMPTS; }

    public static LLMClient getLlmClient() {
        return new LLMClient(llmConfig);
    }

    public static LLMConfig getLlmConfig() {
        return llmConfig;
    }
}
