"""
Utility functions for the Facebook Rental Crawler.
"""
import hashlib
import json
import re
from typing import Optional


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


def get_string_json(text: str) -> Optional[dict]:
    """
    Extract JSON object from a string.
    
    Args:
        text: String containing JSON data
        
    Returns:
        Parsed JSON object as dictionary, or None if not found
    """
    try:
        start = text.find("{")
        if start == -1:
            return None
        
        # Find matching closing brace by counting open/close braces
        brace_count = 0
        end = -1
        for i in range(start, len(text)):
            if text[i] == '{':
                brace_count += 1
            elif text[i] == '}':
                brace_count -= 1
                if brace_count == 0:
                    end = i
                    break
        
        if end == -1:
            return None
        
        json_str = text[start:end + 1]
        return json.loads(json_str)
    except (json.JSONDecodeError, ValueError):
        return None
