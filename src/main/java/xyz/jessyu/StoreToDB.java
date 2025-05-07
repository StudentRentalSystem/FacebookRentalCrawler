package xyz.jessyu;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class StoreToDB {
    private static final String DB_URL = "mongodb://app:house_rental_app$@localhost/house_rental";
    private final MongoClient mongoClient = MongoClients.create(new ConnectionString(DB_URL));

    public static void main(String[] args) {
        StoreToDB storeToDB = new StoreToDB();
        MongoDatabase database = storeToDB.mongoClient.getDatabase("house_rental");
        MongoCollection<Document> collection = database.getCollection("posts");
        Document document = new Document("title", "Sample Title")
                .append("content", "Sample Content")
                .append("author", "Sample Author")
                .append("date", "2023-10-01");
        collection.insertOne(document);
        System.out.println("Document inserted: " + document);
        System.out.println("Database name: " + database.getName());
        System.out.println("Database created: " + database);
        System.out.println("MongoClient created: " + storeToDB.mongoClient);
    }
    public StoreToDB() {


    }
}
