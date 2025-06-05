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
    // These three are for posts content and photos
    private static final String post_photo_relative_xpath = ".//div/*/a/*/*/div/img";
    private static final String post_content_relative_xpath = "//div[@data-ad-preview='message']";
    private static final String[] post_xpath_identifiers = {
            "x1yztbdb", "x1n2onr6", "xh8yej3", "x1ja2u2z"
    };
    private static final String post_xpath_identifier = generate_post_xpath_identifier_from_identifiers(post_xpath_identifiers);

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
        queue = new LinkedBlockingQueue<>();
    }

    public void crawl() {
        try {
            logger.info("Starting Facebook Crawler...");
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
            logger.info("Facebook Crawler finished.");
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            driver.quit();
        }
    }

    // To parse both post content and photos
    private static String generate_post_xpath_identifier_from_identifiers(String[] post_xpath_identifiers) {
        String post_xpath_identifier = "//div[";
        int count = 0;
        for(String c :  post_xpath_identifiers) {
            if(count != 0) post_xpath_identifier += " and ";
            post_xpath_identifier += " contains(@class, " + c + ") ";
            count++;
        }
        post_xpath_identifier = post_xpath_identifier + "]";
        return post_xpath_identifier;
    }

    private void crawlOnePage() {
        List<WebElement> posts = driver.findElements(By.xpath(post_xpath_identifier));
        for(WebElement post : posts) {
            crawlOnePost(post);
        }
    }

    private void crawlOnePost(WebElement element) {
        WebElement seeMore = element.findElement(By.xpath("//div[text()='See more']"));
        // If there's a Chinese version
        if(seeMore == null) {
            seeMore = element.findElement(By.xpath("//div[text()='查看更多']"));
        }
        if(seeMore.isDisplayed()) {
            try {
                js.executeScript("arguments[0].scrollIntoView(true);", seeMore);
                wait.until(ExpectedConditions.elementToBeClickable(seeMore));
                js.executeScript("arguments[0].click();", seeMore);
                Thread.sleep(1000);
            } catch(Exception e) {
                logger.warn("Skip a `See more`");
            }
        }
        String postContent = crawlOnePostContent(element);
        List<String> photoURLs = crawlOnePostPhotoURLs(element);
        if(postContent != null) {
            boolean result = addPost(postContent, photoURLs);
            if(!result) {
                logger.warn("The post has already been added.");
            }
        }
    }

    private String crawlOnePostContent(WebElement element) {
        String text = "";
        try {
            WebElement postContent = element.findElement(By.xpath(post_content_relative_xpath));
            text = postContent.getText();
        } catch (StaleElementReferenceException e) {
            logger.warn("Retry post text extraction due to stale element");
        } catch (Exception e) {
            logger.error("Unexpected error when processing post: " + e.getMessage());
        }
        return text;
    }

    private List<String> crawlOnePostPhotoURLs(WebElement element) {
        List<WebElement> photos = new ArrayList<>();
        List<String> photoURLs = new ArrayList<>();
        try {
            photos = element.findElements(By.xpath(post_photo_relative_xpath));
            for(WebElement photo : photos) {
                String url = photo.getAttribute("src");
                photoURLs.add(url);
            }
        } catch(Exception e) {
            logger.error("Unexpected error when processing post photos: " + e.getMessage());
        }
        return photoURLs;
    }

    private boolean addPost(String content, List<String> photoURLs) {
        String hashContent = Utils.hashContent(content);
        if(!postSet.contains(hashContent)){
            postSet.add(hashContent);
            System.out.println("Hashed content:" + hashContent);
            Post p = new Post(hashContent, content,  photoURLs);
            try {
                queue.put(p);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true;
        }
        return false;
    }

    /**
     * Add a post to the post map.
     * By using SHA-1 hash to check if the post already exists.
     * If the post already exists, it will not be added again.
     * @return the result of the operation.
     * */
    public static class Post {
        public final String id;
        public final String content;
        public final List<String> photoURLs;
        public Post(String id, String content, List<String> photoURLs) {
            this.id = id;
            this.content = content;
            this.photoURLs = photoURLs;
        }

        public static final Post POISON_PILL = new Post(null, null, null);
    }
    //

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


    public BlockingQueue<Post> getQueue() {
        return queue;
    }

}
