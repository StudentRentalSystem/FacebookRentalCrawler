package xyz.jessyu;

import io.github.studentrentalsystem.RentalExtractor;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessPosts {
    private static final String LLM_NAME = Settings.getLLMName();

    public static Document processPost(String post) {
        Document processedPost = null;
        try {
            RentalExtractor extractor = new RentalExtractor();
            JSONObject postJson = extractor.getJSONPost(post, LLM_NAME);
            System.out.println(postJson.toString());
            processedPost = Document.parse(postJson.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return processedPost;
    }

    public static List<Document> processPosts(List<String> posts) {
        List<Document> processedPosts = new ArrayList<>();
        try {
            RentalExtractor extractor = new RentalExtractor();
            for(String post : posts) {
                JSONObject postJson = extractor.getJSONPost(post, LLM_NAME);
                System.out.println(postJson.toString());
                Document doc = Document.parse(postJson.toString());
                processedPosts.add(doc);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return processedPosts;
    }

}
