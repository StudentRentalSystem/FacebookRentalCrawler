package xyz.jessyu;

import org.bson.Document;

import java.util.List;
import java.util.Map;

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
        new Thread(crawler::crawl).start();
        new Thread(()->{
            try {
                while (true) {
                    Crawler.Post post = crawler.getQueue().take();
                    if (post == Crawler.Post.POISON_PILL) break;
                    System.out.println("Processing post: " + post.id);
                    Document processedPost = ProcessPosts.processPost(post.content);
                    StoreToDB.insertPostToDB(processedPost);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}