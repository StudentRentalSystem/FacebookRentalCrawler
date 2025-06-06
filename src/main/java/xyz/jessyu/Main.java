package xyz.jessyu;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
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
        ExecutorService executor = Executors.newFixedThreadPool(scrollCount/2);
        while(true) {
            try {
                Crawler.Post post = crawler.getQueue().take();
                if(post == Crawler.Post.POISON_PILL) break;
                executor.submit(() -> {
                    try {
                        logger.info("Processing post {}", post);
                        Document processedPost = ProcessPosts.processPost(post.content);
                        processedPost.put("照片", post.photoURLs);
                        System.out.println(processedPost.toString());
                        if(processedPost != null) {
                            StoreToDB.insertPostToDB(processedPost);
                        }
                    } catch(Exception e) {
                        logger.error("Error while processing post {}", post, e);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        executor.shutdown();
    }
}