"""
Rental post extractor using LLM for structure extraction.
"""
import json
import logging
from pathlib import Path
from typing import Optional, List
from .llm_client import LLMClient
from .utils import get_string_json
from .settings import Settings


logger = logging.getLogger(__name__)


class RentalExtractor:
    """Extract structured rental information from posts using LLM."""
    
    def __init__(self, max_error_times: int = 5, llm_config: Optional[dict] = None,
                 prompt_path: Optional[str] = None):
        """
        Initialize the rental extractor.
        
        Args:
            max_error_times: Maximum number of retry attempts
            llm_config: Configuration for LLM client
            prompt_path: Path to the prompt template file
        """
        self.max_error_times = max_error_times
        self.prompt_path = prompt_path or Settings.get_prompt_path()
        self.prompt_template = self._load_prompt_template()
        
        if llm_config is None:
            llm_config = Settings.get_llm_config()
        
        self.llm_client = LLMClient(llm_config)
    
    def _load_prompt_template(self) -> str:
        """Load the prompt template from resources."""
        # Try multiple paths to find the prompt template
        
        # First, try from project root (most common case)
        resources_path = Path.cwd() / "src" / "main" / "resources" / self.prompt_path
        if resources_path.exists():
            with open(resources_path, 'r', encoding='utf-8') as f:
                return f.read()
        
        # Try relative to this module file (for package installs)
        module_dir = Path(__file__).parent
        for i in range(6):  # Try up to 6 levels up
            test_path = module_dir
            for _ in range(i):
                test_path = test_path.parent
            resources_path = test_path / "src" / "main" / "resources" / self.prompt_path
            if resources_path.exists():
                with open(resources_path, 'r', encoding='utf-8') as f:
                    return f.read()
        
        # Fallback to current directory
        current_path = Path(self.prompt_path)
        if current_path.exists():
            with open(current_path, 'r', encoding='utf-8') as f:
                return f.read()
        
        logger.warning(f"Prompt template not found at {self.prompt_path}")
        return ""
    
    def call_local_model(self, text: str) -> str:
        """
        Call LLM model to extract rental information.
        
        Args:
            text: The rental post text
            
        Returns:
            LLM response string
        """
        prompt = self.prompt_template.replace("{text}", text)
        response = self.llm_client.call_local_model(prompt)
        return self.llm_client.get_detail_message(response)
    
    def _correct_rental_size(self, rental_sizes: List) -> List:
        """
        Correct rental sizes, marking invalid ones as -1.
        
        Args:
            rental_sizes: List of rental sizes
            
        Returns:
            Corrected list of rental sizes
        """
        revised = []
        for size in rental_sizes:
            try:
                size_float = float(size)
                if size_float >= 100:
                    revised.append(-1)
                else:
                    revised.append(size_float)
            except (ValueError, TypeError):
                revised.append(-1)
        return revised
    
    def _check_first_object(self, arr: List) -> bool:
        """
        Check if array has only one empty or unknown element.
        
        Args:
            arr: List to check
            
        Returns:
            True if array has single empty/unknown element
        """
        return len(arr) == 1 and (arr[0] == "" or arr[0] == "未知")
    
    def _write_default_empty(self, json_obj: dict, key: str, value):
        """
        Write default value if key doesn't exist.
        
        Args:
            json_obj: JSON object to modify
            key: Key to check
            value: Default value to set
        """
        if key not in json_obj:
            json_obj[key] = value
    
    def form_same_json(self, structural_post: dict):
        """
        Normalize JSON structure for contact information.
        
        Args:
            structural_post: The post dictionary to normalize
        """
        contact = structural_post.get("聯絡方式", [])
        result = []
        
        for personal_contact in contact:
            self._write_default_empty(personal_contact, "聯絡人", "")
            self._write_default_empty(personal_contact, "手機", [])
            self._write_default_empty(personal_contact, "lineID", [])
            self._write_default_empty(personal_contact, "lineLink", [])
            self._write_default_empty(personal_contact, "others", [])
            
            name = personal_contact.get("聯絡人", "")
            phone_nums = personal_contact.get("手機", [])
            line_id = personal_contact.get("lineID", [])
            line_link = personal_contact.get("lineLink", [])
            others = personal_contact.get("others", [])
            
            if name == "未知":
                name = ""
            
            if self._check_first_object(phone_nums):
                phone_nums = []
            else:
                # Remove dashes from phone numbers
                modified = []
                for phone in phone_nums:
                    if "-" in phone:
                        modified.append(phone.replace("-", ""))
                    else:
                        modified.append(phone)
                phone_nums = modified
            
            if self._check_first_object(line_id):
                line_id = []
            if self._check_first_object(line_link):
                line_link = []
            if self._check_first_object(others):
                others = []
            
            normalized_contact = {
                "聯絡人": name,
                "手機": phone_nums,
                "lineID": line_id,
                "lineLink": line_link,
                "others": others
            }
            result.append(normalized_contact)
        
        structural_post["聯絡方式"] = result
    
    def get_json_post(self, post: str) -> Optional[dict]:
        """
        Get structured JSON from a rental post.
        
        Args:
            post: The rental post text
            
        Returns:
            Structured JSON object or None
        """
        response = self.call_local_model(post)
        json_object = get_string_json(response)
        
        if json_object and "坪數" in json_object:
            json_object["坪數"] = self._correct_rental_size(json_object["坪數"])
        
        return json_object
    
    def _no_error_json_post(self, post: str) -> Optional[dict]:
        """
        Get JSON post without raising errors.
        
        Args:
            post: The rental post text
            
        Returns:
            Structured JSON object or None
        """
        try:
            response = self.call_local_model(post)
            json_object = get_string_json(response)
            
            if json_object and "坪數" in json_object:
                json_object["坪數"] = self._correct_rental_size(json_object["坪數"])
            
            return json_object
        except Exception as e:
            logger.error(f"Error parsing post: {e}")
            return None
    
    def get_json_post_no_error(self, post: str) -> Optional[dict]:
        """
        Get structured JSON from post with retry logic.
        
        Args:
            post: The rental post text
            
        Returns:
            Structured JSON object or None
        """
        json_post = None
        error_times = 0
        
        while json_post is None and error_times < self.max_error_times:
            json_post = self._no_error_json_post(post)
            if json_post is None:
                error_times += 1
                logger.error(f"Error parsing JSON, attempt {error_times}/{self.max_error_times}")
        
        if json_post is not None:
            self.form_same_json(json_post)
        
        return json_post
    
    def process_posts(self, input_path: str, output_path: str):
        """
        Process multiple rental posts and save to file.
        
        Args:
            input_path: Path to input JSON file with posts
            output_path: Path to output JSON file
        """
        try:
            with open(input_path, 'r', encoding='utf-8') as f:
                posts = json.load(f)
            
            results = []
            
            for i, post in enumerate(posts):
                print(f"⏳ 分析第 {i + 1} 筆貼文...")
                parsed = self.get_json_post_no_error(post)
                results.append(parsed)
            
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(results, f, ensure_ascii=False, indent=2)
            
            print(f"✅ 資料已輸出至 {output_path}")
        
        except Exception as e:
            logger.error(f"Error processing posts: {e}")
