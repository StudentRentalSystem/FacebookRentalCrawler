"""
Basic tests for the Python implementation of Facebook Rental Crawler.
"""
import sys
import os

# Add Python source directory to path
python_src = os.path.join(os.path.dirname(__file__), 'src', 'python')
sys.path.insert(0, python_src)

# Import only non-selenium dependent modules
import xyz.jessyu.utils as utils
import xyz.jessyu.settings as settings_module


def test_hash_content():
    """Test SHA-256 hashing."""
    content = "Test content"
    hash_result = utils.hash_content(content)
    
    # Check hash is 64 characters (SHA-256 hex)
    assert len(hash_result) == 64, f"Expected hash length 64, got {len(hash_result)}"
    
    # Check hash is consistent
    assert utils.hash_content(content) == hash_result, "Hash should be consistent"
    
    # Check different content produces different hash
    assert utils.hash_content("Different content") != hash_result, "Different content should produce different hash"
    
    print("✓ test_hash_content passed")


def test_get_string_json():
    """Test JSON extraction from string."""
    # Test valid JSON
    text = 'Some text {"key": "value", "number": 123} more text'
    result = utils.get_string_json(text)
    assert result is not None, "Should extract JSON"
    assert result["key"] == "value", "Should extract correct value"
    assert result["number"] == 123, "Should extract correct number"
    
    # Test no JSON
    text_no_json = "No JSON here"
    result = utils.get_string_json(text_no_json)
    assert result is None, "Should return None for text without JSON"
    
    # Test nested JSON
    text_nested = '{"outer": {"inner": "value"}}'
    result = utils.get_string_json(text_nested)
    assert result is not None, "Should extract nested JSON"
    assert result["outer"]["inner"] == "value", "Should extract nested value"
    
    print("✓ test_get_string_json passed")


def test_settings():
    """Test Settings configuration."""
    Settings = settings_module.Settings
    
    # Test static methods exist
    assert hasattr(Settings, 'get_chrome_user_data'), "Should have get_chrome_user_data method"
    assert hasattr(Settings, 'get_facebook_url'), "Should have get_facebook_url method"
    assert hasattr(Settings, 'get_llm_config'), "Should have get_llm_config method"
    
    # Test URLs
    assert Settings.get_facebook_url() == "https://www.facebook.com/", "Facebook URL should be correct"
    
    # Test LLM config
    config = Settings.get_llm_config()
    assert isinstance(config, dict), "LLM config should be a dictionary"
    assert "model_type" in config, "LLM config should have model_type"
    
    print("✓ test_settings passed")


if __name__ == "__main__":
    print("Running basic tests for Python implementation...\n")
    
    try:
        test_hash_content()
        test_get_string_json()
        test_settings()
        
        print("\n✅ All tests passed!")
    except AssertionError as e:
        print(f"\n❌ Test failed: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        sys.exit(1)
