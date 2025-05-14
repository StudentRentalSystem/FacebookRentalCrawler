package xyz.jessyu;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>
 *     This class is used to crawl posts from a Facebook group.
 *     It uses Selenium WebDriver to automate the browser and
 *     interact with the Facebook website.
 *     To use this class, follow the steps:
 *     1. Call the constructor to initialize the crawler.
 *     2. Call the crawl() method to start crawling.
 *     3. Call getPostMap() to get the crawled posts.
 * </p>
 * @author JessYu-1011
 * */
public class Crawler {
    private static final String FACEBOOK = Settings.getFacebookUrl();
    private static final String GROUP_URL = Settings.getGroupUrl();
    private static final String CHROME_USER_DATA = Settings.getChromeUserData();
    private int scrollCount;
    private ChromeOptions options;
    private WebDriver driver;
    private Wait<WebDriver> wait;
    private JavascriptExecutor js;
    // This is to help check if the post already exists
    private Set<String> postSet;
    private final BlockingQueue<Post> queue;

    public Crawler(int scrollCount) {
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

        this.scrollCount =  scrollCount;
        wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(3))
                .pollingEvery(Duration.ofMillis(100));
        js = (JavascriptExecutor) driver;
        postSet = new HashSet<>();
        queue = new LinkedBlockingQueue<>();
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
            for(int i = 0 ; i < scrollCount; i++) {
                crawlOnePage();
                scrollDownOnePostEachTime(1);
                Thread.sleep(1000);
            }
            // Notify that the crawling is done
            queue.add(Post.POISON_PILL);
            System.out.println("\nExiting Facebook Crawler...");
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            driver.quit();
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
                System.out.println("Skip a `See more`： " + e.getMessage());
            }
        }
        // Wait for the posts to load
        List<WebElement> postElements = driver.findElements(By.xpath("//div[@data-ad-preview='message']"));

        for (WebElement post : postElements) {
            if(post == null) continue;
            String text = post.getText().trim();
            if (!text.isEmpty() && !text.contains("See more")) {
                System.out.println("------------------------");
                System.out.println(text);
                System.out.println("------------------------");
                boolean result = addPost(text);
                if(!result) {
                    System.out.println("The post has existed.");
                }
            }
        }
    }

    /**
     * Scroll down one post each time.
     * This method is used to scroll down the page to load more posts.
     * */
    private void scrollDownOnePostEachTime(int times) {
        for (int i = 0; i < times; i++) {
            try {
                // The posts in this html
                List<WebElement> posts = driver.findElements(By.xpath("//div[@data-ad-preview='message']"));

                // Check if the posts list is empty
                if (posts.isEmpty()) {
                    System.out.println("Unable to find the posts.");
                    break;
                }

                // Scroll to the last post
                WebElement lastPost = posts.get(posts.size() - 1);

                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'end'});", lastPost);

                Thread.sleep(600);

            } catch (Exception e) {
                System.out.println("Scrolling error：" + e.getMessage());
            }
        }
    }

    /**
     * Add a post to the post map.
     * By using SHA-1 hash to check if the post already exists.
     * If the post already exists, it will not be added again.
     * @return the result of the operation.
     * */
    private boolean addPost(String content) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = digest.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for(byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        String hashContent = sb.toString();
        if(!postSet.contains(hashContent)){
            postSet.add(hashContent);
            Post p = new Post(hashContent, content);
            try {
                queue.put(p);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true;
        }
        return false;
    }

    public BlockingQueue<Post> getQueue() {
        return queue;
    }

    public static class Post {
        public final String id;
        public final String content;
        public Post(String id, String content) {
            this.id = id;
            this.content = content;
        }
        public static final Post POISON_PILL = new Post(null, null);
    }
}
