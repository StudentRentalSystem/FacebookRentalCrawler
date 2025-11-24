"""
Main entry point for Facebook Rental Crawler.
"""
import sys
import logging
import threading
from queue import Empty
from concurrent.futures import ThreadPoolExecutor
from .crawler import Crawler, POISON_PILL
from .process_posts import process_post
from .store_to_db import StoreToDB


# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def main():
    """
    Main function for the Facebook Crawler.
    Initializes the crawler and starts crawling with multi-threaded processing.
    """
    if len(sys.argv) < 2:
        print("Usage: python -m xyz.jessyu.main <SCROLL_COUNT>")
        sys.exit(1)
    
    scroll_count = int(sys.argv[1])
    crawler = Crawler(scroll_count)
    
    # Start crawling in a separate thread
    crawl_thread = threading.Thread(target=crawler.crawl)
    crawl_thread.start()
    
    # Create thread pool for processing posts
    max_workers = max(1, scroll_count // 2)
    executor = ThreadPoolExecutor(max_workers=max_workers)
    
    def process_and_store(post):
        """Process a post and store it to database."""
        try:
            logger.info(f"Processing post {post}")
            processed_post = process_post(post.content)
            
            if processed_post is not None:
                StoreToDB.insert_post_to_db(processed_post)
        except Exception as e:
            logger.error(f"Error while processing post {post}: {e}")
    
    # Process posts from the queue
    while True:
        try:
            # Use timeout to avoid indefinite blocking
            post = crawler.get_queue().get(timeout=5)
            
            if post == POISON_PILL:
                break
            
            executor.submit(process_and_store, post)
        
        except KeyboardInterrupt:
            logger.info("Interrupted by user")
            break
        except Exception:
            # Check if crawler thread is still alive
            if not crawl_thread.is_alive():
                logger.warning("Crawler thread terminated, stopping processing")
                break
            # Otherwise, continue waiting for posts
    
    # Shutdown executor and wait for completion
    executor.shutdown(wait=True)
    crawl_thread.join()
    
    logger.info("Crawler finished successfully")


if __name__ == "__main__":
    main()
