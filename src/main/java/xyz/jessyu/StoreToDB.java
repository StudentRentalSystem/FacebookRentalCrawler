package xyz.jessyu;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.openqa.selenium.json.Json;

import java.util.ArrayList;
import java.util.List;

public class StoreToDB {
    private static final String DB_URL = Settings.getDbUrl();
    public final MongoClient mongoClient = MongoClients.create(new ConnectionString(DB_URL));
    private List<String> posts;
    private List<Json> processedPosts;

    public StoreToDB(List<String> posts) {
        this.posts = posts;
        this.processedPosts = new ArrayList<>();
        processPosts();
    }

    public void processPosts() {

    }

    public void insertPost() {

    }
}
