package xyz.jessyu;

import com.mongodb.ConnectionString;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class FetchAllIds {
    private static final String DB_URL = Settings.getDbUrl();
    private static final String DB_NAME = Settings.getDbName();
    private static final String DB_COLLECTION = Settings.getDbCollection();
    private static final MongoClient mongoClient = MongoClients.create(new ConnectionString(DB_URL));
    private static final MongoCollection<Document> collection = mongoClient.getDatabase(DB_NAME)
            .getCollection(DB_COLLECTION);;

    public static List<String> fetchAllIds() {

        List<String> idList = new ArrayList<>();

        // 只查 `_id` 欄位
        FindIterable<Document> docs = collection.find().projection(new Document("_id", 1));

        for (Document doc : docs) {
            idList.add(doc.get("_id").toString()); // 如果 _id 是 ObjectId 類型
        }

        return idList;
    }
}