"""
MongoDB operations for fetching existing post IDs.
"""
from pymongo import MongoClient
from typing import List
from .settings import Settings


def fetch_all_ids() -> List[str]:
    """
    Fetch all post IDs from MongoDB database.
    
    Returns:
        List of post ID strings
    """
    try:
        db_url = Settings.get_db_url()
        db_name = Settings.get_db_name()
        db_collection = Settings.get_db_collection()
        
        if not db_url:
            return []
        
        client = MongoClient(db_url)
        collection = client[db_name][db_collection]
        
        # Query only the _id field
        docs = collection.find({}, {"_id": 1})
        
        id_list = [str(doc["_id"]) for doc in docs]
        
        client.close()
        return id_list
    except Exception as e:
        print(f"Error fetching IDs from database: {e}")
        return []
