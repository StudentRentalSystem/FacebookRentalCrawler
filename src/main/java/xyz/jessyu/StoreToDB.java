package xyz.jessyu;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.List;

/**
 * <p>Store the posts into MongoDB</p>
 * <p>Usage: Call the insertManyPostToDB function</p>
 * */
public class StoreToDB {
    private static final String DB_URL = Settings.getDbUrl();
    private static final String DB_NAME = Settings.getDbName();
    private static final String DB_COLLECTION = Settings.getDbCollection();
    private static final MongoClient mongoClient = MongoClients.create(new ConnectionString(DB_URL));

    public static void insertPostToDB(Document post) {
        MongoCollection<Document> collection = mongoClient.getDatabase(DB_NAME)
                .getCollection(DB_COLLECTION);
        collection.insertOne(post);
    }

    public static void insertManyPostToDB(List<Document> posts) {
        MongoCollection<Document> collection = mongoClient.getDatabase(DB_NAME)
                .getCollection(DB_COLLECTION);
        collection.insertMany(posts);
    }

}
