package xyz.jessyu;

import com.mongodb.ConnectionString;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Store the posts into MongoDB</p>
 * <p>Usage: Call the insertManyPostToDB function</p>
 * */
public class StoreToDB {
    private static final String DB_URL = Settings.getDbUrl();
    private static final String DB_NAME = Settings.getDbName();
    private static final String DB_COLLECTION = Settings.getDbCollection();
    private static final MongoClient mongoClient = MongoClients.create(new ConnectionString(DB_URL));
    private static final Logger logger = LoggerFactory.getLogger(StoreToDB.class);

    public static void insertPostToDB(Document post) {
        logger.info("Inserting post into DB");
        MongoCollection<Document> collection = mongoClient.getDatabase(DB_NAME)
                .getCollection(DB_COLLECTION);
        try {
            collection.insertOne(post);
        } catch(MongoWriteException e) {
            logger.error("Duplicate key found in database");
            System.out.println("Duplicate key found in database");
        }
    }
}
