package xyz.jessyu;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.JSONException;
import io.github.studentrentalsystem.RentalExtractor;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StoreToDB {
    private static final String DB_URL = Settings.getDbUrl();
    public final MongoClient mongoClient = MongoClients.create(new ConnectionString(DB_URL));
    private List<String> posts;
    private List<Document> processedPosts;

    public StoreToDB(List<String> posts) {
        this.posts = posts;
        this.processedPosts = new ArrayList<>();
        processPosts();
    }

    private void processPosts() {
        try {
            RentalExtractor extractor = new RentalExtractor();
            for(String post : posts) {
                JSONObject postJson = extractor.getJSONPost(post, "llama3:8b");
                System.out.println(postJson.toString());
                Document doc = Document.parse(postJson.toString());
                processedPosts.add(doc);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public void insertPostToDB() {
        MongoCollection<Document> collection = mongoClient.getDatabase("app")
                .getCollection("house_rental");
        collection.insertMany(processedPosts);
    }
}
