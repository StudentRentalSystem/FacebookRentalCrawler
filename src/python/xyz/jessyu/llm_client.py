"""
LLM client for interacting with Ollama server.
"""
import requests
import json
import logging


logger = logging.getLogger(__name__)


class LLMClient:
    """Client for interacting with local LLM (Ollama) server."""
    
    def __init__(self, config: dict):
        """
        Initialize LLM client.
        
        Args:
            config: Dictionary containing server_address, server_port, and model_type
        """
        self.server_address = config.get("server_address", "http://localhost")
        self.server_port = config.get("server_port", 11434)
        self.model_type = config.get("model_type", "llama3:8b")
        self.base_url = f"{self.server_address}:{self.server_port}"
    
    def call_local_model(self, prompt: str) -> str:
        """
        Call the local LLM model with a prompt.
        
        Args:
            prompt: The prompt to send to the model
            
        Returns:
            The model's response as a string
        """
        try:
            url = f"{self.base_url}/api/generate"
            
            payload = {
                "model": self.model_type,
                "prompt": prompt,
                "stream": False
            }
            
            response = requests.post(url, json=payload, timeout=300)
            response.raise_for_status()
            
            result = response.json()
            return result.get("response", "")
        except requests.exceptions.RequestException as e:
            logger.error(f"Error calling LLM model: {e}")
            return ""
    
    def get_detail_message(self, response: str) -> str:
        """
        Extract detailed message from response.
        
        Args:
            response: Raw response from the model
            
        Returns:
            Processed response string
        """
        # In the Java version, this extracts the message from a complex JSON
        # For simplicity, we just return the response as-is
        return response
    
    def call_chat_model(self, messages: list) -> str:
        """
        Call the chat model with a list of messages.
        
        Args:
            messages: List of message dictionaries with 'role' and 'content'
            
        Returns:
            The model's response as a string
        """
        try:
            url = f"{self.base_url}/api/chat"
            
            payload = {
                "model": self.model_type,
                "messages": messages,
                "stream": False
            }
            
            response = requests.post(url, json=payload, timeout=300)
            response.raise_for_status()
            
            result = response.json()
            return result.get("message", {}).get("content", "")
        except requests.exceptions.RequestException as e:
            logger.error(f"Error calling chat model: {e}")
            return ""
