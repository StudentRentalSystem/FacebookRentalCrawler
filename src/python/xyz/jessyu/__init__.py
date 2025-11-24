"""
Facebook Rental Crawler Package.

A Facebook group crawler powered by Selenium that extracts rental posts,
processes them using LLM, and stores them in MongoDB.
"""

__version__ = "1.0.0"
__author__ = "JessYu-1011, hding4915"

from .crawler import Crawler, Post, POISON_PILL
from .settings import Settings
from .utils import hash_content, get_string_json
from .fetch_all_ids import fetch_all_ids
from .store_to_db import StoreToDB
from .process_posts import process_post
from .rental_extractor import RentalExtractor
from .llm_client import LLMClient

__all__ = [
    'Crawler',
    'Post',
    'POISON_PILL',
    'Settings',
    'hash_content',
    'get_string_json',
    'fetch_all_ids',
    'StoreToDB',
    'process_post',
    'RentalExtractor',
    'LLMClient',
]
