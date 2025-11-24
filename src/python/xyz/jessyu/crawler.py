"""
Facebook group crawler using Selenium WebDriver.
"""
import time
import logging
from queue import Queue
from typing import Set, List, Optional
from dataclasses import dataclass
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import (
    StaleElementReferenceException,
    TimeoutException,
    NoSuchElementException
)
from .settings import Settings
from .utils import hash_content
from .fetch_all_ids import fetch_all_ids


logger = logging.getLogger(__name__)


@dataclass
class Post:
    """Represents a crawled post."""
    id: str
    content: str


# Poison pill sentinel for queue
POISON_PILL = Post(id=None, content=None)


class Crawler:
    """
    Facebook group crawler that extracts rental posts.
    
    Usage:
        1. Create crawler instance with scroll_count
        2. Call crawl() method to start crawling
        3. Get posts from the queue using get_queue()
    """
    
    def __init__(self, scroll_count: int):
        """
        Initialize the crawler.
        
        Args:
            scroll_count: Number of times to scroll down the page
        """
        self.facebook_url = Settings.get_facebook_url()
        self.group_url = Settings.get_group_url()
        self.chrome_user_data = Settings.get_chrome_user_data()
        self.scroll_count = scroll_count
        
        logger.info("Starting Crawler")
        logger.info(f"Group URL: {self.group_url}")
        logger.info(f"Chrome User Data: {self.chrome_user_data}")
        
        # Setup Chrome options
        self.options = Options()
        self.options.add_argument(f"user-data-dir={self.chrome_user_data}")
        self.options.add_argument("profile-directory=Default")
        
        # Initialize WebDriver
        self.driver = webdriver.Chrome(options=self.options)
        self.wait = WebDriverWait(self.driver, 3)
        
        # Initialize post tracking
        self.post_set: Set[str] = set()
        self.id_list: List[str] = fetch_all_ids()
        self.post_set.update(self.id_list)
        
        # Initialize queue for posts
        self.queue: Queue[Post] = Queue()
        
        logger.info("Facebook Crawler initialized.")
    
    def crawl(self):
        """Start crawling the Facebook group."""
        try:
            logger.info("Starting Facebook Crawler...")
            
            # Navigate to Facebook
            self.driver.get(self.facebook_url)
            self._wait_for_url(self.facebook_url)
            
            # Navigate to group
            self.driver.get(self.group_url)
            self._wait_for_url(self.group_url)
            
            same_post_count = 0
            last_post_set_size = 0
            re_scroll_times = 0
            
            for i in range(self.scroll_count):
                self._crawl_one_page()
                self._scroll_down_one_post_each_time(1)
                time.sleep(1)
                
                current_post_set_size = len(self.post_set)
                print(f"{current_post_set_size}, {last_post_set_size}")
                
                if current_post_set_size == last_post_set_size:
                    same_post_count += 1
                    print(f"No new posts found. samePostCount = {same_post_count}")
                    
                    if same_post_count >= 2:
                        print("Detected stagnant post set. Scrolling extra times to force refresh...")
                        self._force_scroll_down(re_scroll_times + 1, 2000)
                        re_scroll_times += 1
                        same_post_count = 0
                else:
                    same_post_count = 0
                    re_scroll_times = 0
                
                last_post_set_size = current_post_set_size
            
            # Add poison pill to signal completion
            self.queue.put(POISON_PILL)
            logger.info("Facebook Crawler finished.")
        
        except Exception as e:
            logger.error(f"Error during crawling: {e}")
            self.queue.put(POISON_PILL)
        
        finally:
            self.driver.quit()
    
    def _wait_for_url(self, url: str):
        """Wait for URL to contain the specified string."""
        try:
            self.wait.until(EC.url_contains(url))
        except TimeoutException:
            logger.warning(f"Timeout waiting for URL: {url}")
    
    def _crawl_one_page(self):
        """Crawl posts on the current page."""
        try:
            # Find and click all "See More" buttons (查看更多)
            see_more_buttons = self.driver.find_elements(
                By.XPATH, "//div[text()='查看更多']"
            )
            
            for button in see_more_buttons:
                try:
                    if button.is_displayed():
                        self.driver.execute_script(
                            "arguments[0].scrollIntoView(true);", button
                        )
                        time.sleep(0.5)
                        self.driver.execute_script("arguments[0].click();", button)
                        time.sleep(1)
                except Exception as e:
                    logger.warning("Skip a `查看更多`")
            
            time.sleep(1)
            
            # Find all posts
            post_elements = self.driver.find_elements(
                By.XPATH, "//div[@data-ad-preview='message']"
            )
            
            for post in post_elements:
                success = False
                
                for retry in range(3):
                    if success:
                        break
                    
                    try:
                        if post is None:
                            continue
                        
                        text = post.text.strip()
                        
                        if text and "查看更多" not in text:
                            hash_content_str = hash_content(text)
                            
                            if hash_content_str in self.post_set:
                                print("跳過重複貼文")
                                success = True
                                continue
                            
                            print("------------------------")
                            print(text)
                            print("------------------------")
                            
                            result = self._add_post(text, hash_content_str)
                            if not result:
                                logger.info("The post has existed")
                        
                        success = True
                    
                    except StaleElementReferenceException:
                        logger.warning("Retry post text extraction due to stale element")
                        time.sleep(0.5)
                    
                    except Exception as e:
                        logger.error(f"Unexpected error when processing post: {e}")
                        break
        
        except Exception as e:
            logger.error(f"Error crawling page: {e}")
    
    def _force_scroll_down(self, times: int, size: int):
        """
        Force scroll down the page.
        
        Args:
            times: Number of times to scroll
            size: Pixels to scroll each time
        """
        for i in range(times):
            try:
                self.driver.execute_script(f"window.scrollBy(0, {size});")
                time.sleep(1)
            except Exception as e:
                logger.error(f"Force scroll error: {e}")
    
    def _scroll_down_one_post_each_time(self, times: int):
        """
        Scroll down one post each time.
        
        Args:
            times: Number of times to scroll
        """
        for i in range(times):
            try:
                # Find all posts
                posts = self.driver.find_elements(
                    By.XPATH, "//div[@data-ad-preview='message']"
                )
                
                if not posts:
                    logger.warning("Unable to find the posts")
                    break
                
                # Scroll to the last post
                last_post = posts[-1]
                self.driver.execute_script(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'end'});",
                    last_post
                )
                
                time.sleep(0.6)
            
            except Exception as e:
                logger.error("Scrolling Error")
    
    def _add_post(self, content: str, hash_content_str: str) -> bool:
        """
        Add a post to the post set and queue.
        
        Args:
            content: Post content
            hash_content_str: SHA-256 hash of the content
            
        Returns:
            True if post was added, False if it already exists
        """
        if hash_content_str not in self.post_set:
            self.post_set.add(hash_content_str)
            print(f"Hashed content: {hash_content_str}")
            
            post = Post(id=hash_content_str, content=content)
            self.queue.put(post)
            
            return True
        
        return False
    
    def get_queue(self) -> Queue[Post]:
        """
        Get the queue containing crawled posts.
        
        Returns:
            Queue of Post objects
        """
        return self.queue
