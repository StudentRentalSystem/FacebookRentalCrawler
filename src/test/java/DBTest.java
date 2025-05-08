import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import xyz.jessyu.StoreToDB;

public class DBTest {
        public static void main(String[] args) {
            StoreToDB storeToDB = new StoreToDB(null);
            MongoDatabase database = storeToDB.mongoClient.getDatabase("app");
            MongoCollection<Document> collection = database.getCollection("house_rental");
            Document houseRental = new Document("租金", 500);
            InsertOneResult insertOneResult = collection.insertOne(houseRental);
            System.out.println(insertOneResult.getInsertedId().asObjectId().getValue());
        }
}
