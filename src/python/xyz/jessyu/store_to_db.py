"""
MongoDB storage operations for rental posts.
"""
from pymongo import MongoClient
from pymongo.errors import DuplicateKeyError
import logging
from .settings import Settings


logger = logging.getLogger(__name__)


# Singleton MongoDB client
_mongo_client = None
_collection = None


def _get_collection():
    """Get or create MongoDB collection."""
    global _mongo_client, _collection
    
    if _collection is None:
        db_url = Settings.get_db_url()
        db_name = Settings.get_db_name()
        db_collection = Settings.get_db_collection()
        
        _mongo_client = MongoClient(db_url)
        _collection = _mongo_client[db_name][db_collection]
    
    return _collection


class StoreToDB:
    """Handle storing rental posts to MongoDB."""
    
    @staticmethod
    def insert_post_to_db(post: dict):
        """
        Insert a single post into MongoDB.
        
        Args:
            post: Dictionary containing post data
        """
        logger.info("Inserting post into DB")
        
        try:
            collection = _get_collection()
            collection.insert_one(post)
            logger.info("Post inserted successfully")
        except DuplicateKeyError:
            logger.error("Duplicate key found in database")
        except Exception as e:
            logger.error(f"Error inserting post to database: {e}")
