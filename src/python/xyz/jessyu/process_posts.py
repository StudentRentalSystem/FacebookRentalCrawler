"""
Post processing module for rental posts.
"""
import logging
from typing import Optional
from .rental_extractor import RentalExtractor
from .utils import hash_content
from .settings import Settings


logger = logging.getLogger(__name__)


def process_post(post: str) -> Optional[dict]:
    """
    Process a single rental post.
    
    Args:
        post: The rental post text
        
    Returns:
        Processed post as dictionary or None
    """
    processed_post = None
    attempts = 0
    success = False
    
    logger.info(f"Processing post: {post}")
    
    while attempts < Settings.get_retry_attempts() and not success:
        try:
            extractor = RentalExtractor(5, Settings.get_llm_config())
            post_json = extractor.get_json_post_no_error(post)
            
            if post_json:
                print(post_json)
                processed_post = post_json
                processed_post["_id"] = hash_content(post)
                success = True
        except Exception as e:
            logger.error(f"Error processing post: {e}")
            attempts += 1
    
    return processed_post
