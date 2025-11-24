"""
Facebook Rental Crawler Package.

A Facebook group crawler powered by Selenium that extracts rental posts,
processes them using LLM, and stores them in MongoDB.
"""

__version__ = "1.0.0"
__author__ = "JessYu-1011, hding4915"

# Note: Imports are lazy to avoid requiring all dependencies
# Import the modules you need directly, e.g.:
# from xyz.jessyu.crawler import Crawler
# from xyz.jessyu.settings import Settings

__all__ = [
    'crawler',
    'settings',
    'utils',
    'fetch_all_ids',
    'store_to_db',
    'process_posts',
    'rental_extractor',
    'llm_client',
]
