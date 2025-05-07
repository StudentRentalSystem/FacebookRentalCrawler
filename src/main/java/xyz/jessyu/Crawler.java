package xyz.jessyu;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;


import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Crawler {
    private static final String FACEBOOK = Settings.getFacebookUrl();
    private static final String GROUP_URL = Settings.getGroupUrl();
    private static final String CHROME_USER_DATA = Settings.getChromeUserData();
    private ChromeOptions options;
    private WebDriver driver;
    private Wait<WebDriver> wait;
    private JavascriptExecutor js;
    private ArrayList<String> postList;

    public Crawler() {
        System.out.println("Initializing Facebook Crawler...");
        System.out.println("Facebook URL: " + FACEBOOK);
        System.out.println("Group URL: " + GROUP_URL);
        System.out.println("Chrome User Data Directory: " + CHROME_USER_DATA);
        options = new ChromeOptions();
        options.addArguments("user-data-dir=" + CHROME_USER_DATA);
        options.addArguments("profile-directory=Default");

        System.out.println("Chrome Profile Directory: Default");
        driver = new ChromeDriver(options);

        System.out.println("Facebook Crawler initialized.");

        wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(3))
                .pollingEvery(Duration.ofMillis(100));
        js = (JavascriptExecutor) driver;
        postList = new ArrayList<>();
    }

    public void crawl() {
        try {
            System.out.println("Starting Facebook Crawler...");
            driver.get(FACEBOOK);
            wait.until(
                    ExpectedConditions.urlContains(FACEBOOK)
            );

            driver.get(GROUP_URL);
            wait.until(
                    ExpectedConditions.urlContains(GROUP_URL)
            );

            // Scroll one post each time
            for(int i = 0 ; i < 10; i++) {
                crawlOnePage();
                scrollDownOnePostEachTime(1);
                Thread.sleep(1000);
            }
            System.out.println("\nPress Enter to exit.");
            System.in.read();
        } catch (IOException e) {
            e.getMessage();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }

        // Delete the last post
        if (!postList.isEmpty()) {
            postList.remove(postList.size() - 1);
        }


    }

    private void crawlOnePage() {
        List<WebElement> seeMoreButtons = driver.findElements(By.xpath("//div[text()='See more']"));
        // Expand all "See more" buttons
        for (WebElement button : seeMoreButtons) {
            try {
                if (button.isDisplayed()) {
                    js.executeScript("arguments[0].scrollIntoView(true);", button);
                    wait.until(ExpectedConditions.elementToBeClickable(button));
                    js.executeScript("arguments[0].click();", button);
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                System.out.println("跳過一個 `See more`： " + e.getMessage());
            }
        }
        // Wait for the posts to load
        List<WebElement> postElements = driver.findElements(By.xpath("//div[@data-ad-preview='message']"));

        for (WebElement post : postElements) {
            String text = post.getText().trim();
            if (!text.isEmpty()) {
                System.out.println("------------------------");
                System.out.println(text);
                System.out.println("------------------------");
                postList.add(text);
            }
        }
    }

    public void scrollDownOnePostEachTime(int times) {
        for (int i = 0; i < times; i++) {
            try {
                // The posts in this html
                List<WebElement> posts = driver.findElements(By.xpath("//div[@data-ad-preview='message']"));

                // Check if the posts list is empty
                if (posts.isEmpty()) {
                    System.out.println("找不到貼文元素，跳過滾動。");
                    break;
                }

                // Scroll to the last post
                WebElement lastPost = posts.get(posts.size() - 1);

                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'end'});", lastPost);

                Thread.sleep(600);

            } catch (Exception e) {
                System.out.println("滾動錯誤：" + e.getMessage());
            }
        }
    }

}
