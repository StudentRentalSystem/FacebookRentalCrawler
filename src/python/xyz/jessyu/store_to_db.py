"""
MongoDB storage operations for rental posts.
"""
from pymongo import MongoClient
from pymongo.errors import DuplicateKeyError
import logging
from .settings import Settings


logger = logging.getLogger(__name__)


class StoreToDB:
    """Handle storing rental posts to MongoDB."""
    
    def __init__(self):
        """Initialize MongoDB connection."""
        db_url = Settings.get_db_url()
        db_name = Settings.get_db_name()
        db_collection = Settings.get_db_collection()
        
        self.client = MongoClient(db_url)
        self.collection = self.client[db_name][db_collection]
    
    @staticmethod
    def insert_post_to_db(post: dict):
        """
        Insert a single post into MongoDB.
        
        Args:
            post: Dictionary containing post data
        """
        logger.info("Inserting post into DB")
        
        try:
            db_url = Settings.get_db_url()
            db_name = Settings.get_db_name()
            db_collection = Settings.get_db_collection()
            
            client = MongoClient(db_url)
            collection = client[db_name][db_collection]
            
            collection.insert_one(post)
            logger.info("Post inserted successfully")
            
            client.close()
        except DuplicateKeyError:
            logger.error("Duplicate key found in database")
            print("Duplicate key found in database")
        except Exception as e:
            logger.error(f"Error inserting post to database: {e}")
