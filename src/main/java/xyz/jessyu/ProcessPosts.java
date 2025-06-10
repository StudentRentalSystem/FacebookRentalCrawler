package xyz.jessyu;
import io.github.studentrentalsystem.RentalExtractor;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProcessPosts {
    private static final Logger logger = LoggerFactory.getLogger(ProcessPosts.class);

    public static Document processPost(String post) {
        Document processedPost = null;
        JSONObject postJson = null;
        int attempts = 0;
        boolean success = false;
        logger.info("Processing post: " + post);
        while(attempts < Settings.getRetryAttempts() && !success){
            try {
                RentalExtractor extractor = new RentalExtractor(5, Settings.getLlmConfig());
                postJson = extractor.getJSONPostNoError(post);

                System.out.println(postJson.toString());
                processedPost = Document.parse(postJson.toString());
                processedPost.put("_id", Utils.hashContent(post));
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch(JSONException e) {
                attempts++;
                logger.error("Error parsing JSON");
            }
        }
        return processedPost;
    }
}
