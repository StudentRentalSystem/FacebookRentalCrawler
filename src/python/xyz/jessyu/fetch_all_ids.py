"""
MongoDB operations for fetching existing post IDs.
"""
from pymongo import MongoClient
from typing import List
from .settings import Settings


# Reuse the MongoDB client from store_to_db
def _get_mongo_client():
    """Get or create MongoDB client singleton."""
    from .store_to_db import _get_collection
    return _get_collection()


def fetch_all_ids() -> List[str]:
    """
    Fetch all post IDs from MongoDB database.
    
    Returns:
        List of post ID strings
    """
    try:
        db_url = Settings.get_db_url()
        
        if not db_url:
            return []
        
        collection = _get_mongo_client()
        
        # Query only the _id field
        docs = collection.find({}, {"_id": 1})
        
        id_list = [str(doc["_id"]) for doc in docs]
        
        return id_list
    except Exception as e:
        print(f"Error fetching IDs from database: {e}")
        return []
