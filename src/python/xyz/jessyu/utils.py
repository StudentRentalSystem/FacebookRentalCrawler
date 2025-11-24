"""
Utility functions for the Facebook Rental Crawler.
"""
import hashlib
import json
import re


def hash_content(content: str) -> str:
    """
    Hash content using SHA-256.
    
    Args:
        content: The content to hash
        
    Returns:
        Hexadecimal string representation of the hash
    """
    digest = hashlib.sha256()
    digest.update(content.encode('utf-8'))
    return digest.hexdigest()


def get_string_json(text: str) -> dict:
    """
    Extract JSON object from a string.
    
    Args:
        text: String containing JSON data
        
    Returns:
        Parsed JSON object as dictionary, or None if not found
    """
    try:
        start = text.find("{")
        end = text.rfind("}")
        
        if start == -1 or end == -1:
            return None
        
        json_str = text[start:end + 1]
        return json.loads(json_str)
    except (json.JSONDecodeError, ValueError):
        return None
