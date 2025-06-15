package xyz.jessyu;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private int scrollCount;
    private ChromeOptions options;
    private WebDriver driver;
    private Wait<WebDriver> wait;
    private JavascriptExecutor js;
    // This is to help check if the post already exists
    private Set<String> postSet;
    private final BlockingQueue<Post> queue;
    private final List<String> idList = FetchAllIds.fetchAllIds();

    public Crawler(int scrollCount) {
        logger.info("Starting Crawler");
        logger.info("Group URL: {}", GROUP_URL);
        logger.info("Chrome User Data: {}", CHROME_USER_DATA);

        options = new ChromeOptions();
        options.addArguments("user-data-dir=" + CHROME_USER_DATA);
        options.addArguments("profile-directory=Default");

        driver = new ChromeDriver(options);

        logger.info("Facebook Crawler initialized.");
        this.scrollCount =  scrollCount;
        wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(3))
                .pollingEvery(Duration.ofMillis(100));
        js = (JavascriptExecutor) driver;
        postSet = new HashSet<>();
        postSet.addAll(idList);
        queue = new LinkedBlockingQueue<>();
    }

    public void crawl() {
        try {
            logger.info("Starting Facebook Crawler...");
            driver.get(FACEBOOK);
            wait.until(ExpectedConditions.urlContains(FACEBOOK));
            driver.get(GROUP_URL);
            wait.until(ExpectedConditions.urlContains(GROUP_URL));

            int samePostCount = 0;  // 新增變數來記錄連續無新貼文的次數
            int lastPostSetSize = 0;
            int reScrollTimes = 0;

            for (int i = 0 ; i < scrollCount; i++) {
                crawlOnePage();
                scrollDownOnePostEachTime(1);
                Thread.sleep(1000);

                int currentPostSetSize = postSet.size();
                System.out.println(currentPostSetSize + ", " + lastPostSetSize);
                if (currentPostSetSize == lastPostSetSize) {
                    samePostCount++;
                    System.out.println("No new posts found. samePostCount = " + samePostCount);
                    if (samePostCount >= 2) {
                        System.out.println("Detected stagnant post set. Scrolling extra times to force refresh...");
                        forceScrollDown(reScrollTimes + 1, 2000);
                        reScrollTimes++;
                        samePostCount = 0;
                    }
                } else {
                    samePostCount = 0;
                    reScrollTimes = 0;
                }
                lastPostSetSize = currentPostSetSize;
            }

            queue.add(Post.POISON_PILL);
            logger.info("Facebook Crawler finished.");
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            driver.quit();
        }
    }


    private void crawlOnePage() throws InterruptedException {
        List<WebElement> seeMoreButtons = driver.findElements(By.xpath("//div[text()='查看更多']"));
        // Expand all "查看更多" buttons
        for (WebElement button : seeMoreButtons) {
            try {
                if (button.isDisplayed()) {
                    js.executeScript("arguments[0].scrollIntoView(true);", button);
                    wait.until(ExpectedConditions.elementToBeClickable(button));
                    js.executeScript("arguments[0].click();", button);
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                logger.warn("Skip a `查看更多`");
            }
        }
        Thread.sleep(1000);
        // Wait for the posts to load
        List<WebElement> postElements = driver.findElements(By.xpath("//div[@data-ad-preview='message']"));

        for (WebElement post : postElements) {
            boolean success = false;
            for (int retry = 0; retry < 3 && !success; retry++) {
                try {
                    if (post == null) continue;

                    String text = post.getText().trim();
                    if (!text.isEmpty() && !text.contains("查看更多")) {
                        String hashContent = Utils.hashContent(text);
                        if (postSet.contains(hashContent)) {
                            System.out.println("跳過重複貼文");
                            continue;
                        }

                        System.out.println("------------------------");
                        System.out.println(text);
                        System.out.println("------------------------");

                        boolean result = addPost(text, hashContent);
                        if (!result) {
                            logger.info("The post has existed");
                        }
                    }
                    success = true;
                } catch (StaleElementReferenceException e) {
                    logger.warn("Retry post text extraction due to stale element");
                    Thread.sleep(500);
                } catch (Exception e) {
                    logger.error("Unexpected error when processing post: " + e.getMessage());
                    break;
                }
            }
        }
    }

    private void forceScrollDown(int times, int size) {
        for (int i = 0; i < times; i++) {
            try {
                js.executeScript(String.format("window.scrollBy(0, %d);", size));
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("Force scroll error: " + e.getMessage());
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
                    logger.warn("Unable to find the posts");
                    break;
                }

                // Scroll to the last post
                WebElement lastPost = posts.get(posts.size() - 1);

                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'end'});", lastPost);

                Thread.sleep(600);

            } catch (Exception e) {
                logger.error("Scrolling Error");
            }
        }
    }

    /**
     * Add a post to the post map.
     * By using SHA-1 hash to check if the post already exists.
     * If the post already exists, it will not be added again.
     * @return the result of the operation.
     * */
    private boolean addPost(String content, String hashContent) {
        if(!postSet.contains(hashContent)){
            postSet.add(hashContent);
            System.out.println("Hashed content:" + hashContent);
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
