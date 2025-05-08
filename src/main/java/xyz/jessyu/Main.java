package xyz.jessyu;

import java.util.List;

public class Main {
    /**
     * <p>
     *     This is the main class for the Facebook Crawler.
     *     It initializes the crawler and starts crawling.
     * </p>
     * @param args command line arguments, the first argument is the scroll count
     */
    public static void main(String[] args) {
        int scrollCount = Integer.parseInt(args[0]);
        Crawler crawler = new Crawler(scrollCount);
        crawler.crawl();
        List<String> posts = crawler.getPostsList();
        StoreToDB sTDB = new StoreToDB(posts);
        sTDB.insertPostToDB();
    }
}