package xyz.jessyu;

import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.List;

public class Crawler {
    private static final String FACEBOOK = Settings.getFacebookUrl();
    private static final String GROUP_URL = Settings.getGroupUrl();
    private static final String CHROME_USER_DATA = Settings.getChromeUserData();
    private ChromeOptions options;
    private WebDriver driver;

    public Crawler() {
        options = new ChromeOptions();
        options.addArguments("user-data-dir=" + CHROME_USER_DATA);
        options.addArguments("profile-directory=Default");

        driver = new ChromeDriver(options);
    }

    public void crawl() {
        try {
            driver.get(FACEBOOK);
            System.out.println("After Login Facebook, press enter on the keyboard to continue:");
            System.in.read();

            driver.get(GROUP_URL);
            Thread.sleep(3000);

            for (int i = 0; i < 5; i++) {
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
                Thread.sleep(3000);
            }

            List<WebElement> seeMoreButtons = driver.findElements(By.xpath("//div[text()='See more']"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            for (WebElement button : seeMoreButtons) {
                try {
                    if (button.isDisplayed()) {
                        js.executeScript("arguments[0].scrollIntoView(true);", button);
                        Thread.sleep(500);
                        js.executeScript("arguments[0].click();", button);
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    System.out.println("跳過一個 `See more`： " + e.getMessage());
                }
            }

            String page = driver.getPageSource();
            Document doc = org.jsoup.Jsoup.parse(page);

            List<WebElement> postElements = driver.findElements(By.xpath("//div[@data-ad-preview='message']"));

            int index = 1;
            for (WebElement post : postElements) {
                String text = post.getText().trim();
                if (!text.isEmpty()) {
                    System.out.println(index++ + ". " + text);
                }
            }

            System.out.println("\nPress Enter to exit.");
            System.in.read();

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

}
