"""
Settings module for Facebook Rental Crawler.
Handles configuration and environment variables.
"""
import os
from pathlib import Path


class Settings:
    """Configuration settings for the Facebook Rental Crawler."""
    
    # File paths
    PROMPT_PATH = "extract_prompt.txt"
    RENTAL_POSTS_PATH = "rental_posts.json"
    EXTRACTED_DATA_PATH = "extracted_data.json"
    OUTPUT_PATH = os.path.join("src", "main", "resources", EXTRACTED_DATA_PATH)
    
    # URLs and Database
    FACEBOOK_URL = "https://www.facebook.com/"
    GROUP_URL = os.getenv("FACEBOOK_GROUP_URL")
    DB_URL = os.getenv("DB_URL")
    DB_NAME = "app"
    DB_COLLECTION = "house_rental"
    
    # LLM Configuration
    LLM_MODEL_TYPE = "llama3:8b"
    LLM_SERVER_ADDRESS = os.getenv("LLM_SERVER_ADDRESS")
    LLM_SERVER_PORT = int(os.getenv("LLM_SERVER_PORT", "11434"))
    RETRY_ATTEMPTS = 1
    
    @staticmethod
    def get_chrome_user_data():
        """Get Chrome user data directory based on OS."""
        user_home = Path.home()
        os_name = os.name
        
        if os_name == 'nt':  # Windows
            chrome_user_data = user_home / "fb-crawler"
        elif os_name == 'posix':  # macOS and Linux
            chrome_user_data = user_home / "fb-crawler"
        else:
            raise OSError(f"Unsupported OS: {os_name}")
        
        return str(chrome_user_data)
    
    @staticmethod
    def get_prompt_path():
        """Get the path to the prompt template file."""
        return Settings.PROMPT_PATH
    
    @staticmethod
    def get_rental_posts_path():
        """Get the path to rental posts file."""
        return Settings.RENTAL_POSTS_PATH
    
    @staticmethod
    def get_extracted_data_path():
        """Get the path to extracted data file."""
        return Settings.EXTRACTED_DATA_PATH
    
    @staticmethod
    def get_output_path():
        """Get the output path for extracted data."""
        return Settings.OUTPUT_PATH
    
    @staticmethod
    def get_facebook_url():
        """Get Facebook main URL."""
        return Settings.FACEBOOK_URL
    
    @staticmethod
    def get_group_url():
        """Get Facebook group URL."""
        return Settings.GROUP_URL
    
    @staticmethod
    def get_db_url():
        """Get MongoDB connection URL."""
        return Settings.DB_URL
    
    @staticmethod
    def get_db_name():
        """Get MongoDB database name."""
        return Settings.DB_NAME
    
    @staticmethod
    def get_db_collection():
        """Get MongoDB collection name."""
        return Settings.DB_COLLECTION
    
    @staticmethod
    def get_llm_model_type():
        """Get LLM model type."""
        return Settings.LLM_MODEL_TYPE
    
    @staticmethod
    def get_retry_attempts():
        """Get retry attempts for processing."""
        return Settings.RETRY_ATTEMPTS
    
    @staticmethod
    def get_llm_config():
        """Get LLM configuration dictionary."""
        return {
            "server_address": Settings.LLM_SERVER_ADDRESS,
            "server_port": Settings.LLM_SERVER_PORT,
            "model_type": Settings.LLM_MODEL_TYPE
        }
