# Python Translation - Quick Start Guide

This guide helps you get started with the Python version of the Facebook Rental Crawler.

## Prerequisites

1. **Python 3.8+** installed
2. **Chrome browser** with matching ChromeDriver
3. **Ollama** server running locally
4. **MongoDB** (optional, for storing results)
5. **Facebook account** logged into Chrome

## Installation

### Step 1: Install Dependencies

```bash
pip install -r requirements.txt
```

Or use setup.py:

```bash
pip install -e .
```

### Step 2: Set Up Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` and configure:

```env
FACEBOOK_GROUP_URL=https://www.facebook.com/groups/YOUR_GROUP_ID
DB_URL=mongodb://localhost:27017
LLM_SERVER_ADDRESS=http://localhost
LLM_SERVER_PORT=11434
```

### Step 3: Set Up Ollama

Install and run Ollama, then pull the required model:

```bash
ollama pull llama3:8b
```

### Step 4: Prepare Chrome Profile

The crawler uses your Chrome user profile to access Facebook. Create a profile directory:

```bash
# Linux/macOS
mkdir -p ~/fb-crawler

# Windows
# Create folder: %USERPROFILE%\fb-crawler
```

Log into Facebook using Chrome with this profile before running the crawler.

## Running the Crawler

### Option 1: Using Helper Scripts (Recommended)

**Linux/macOS:**
```bash
./run_python.sh 10
```

**Windows:**
```bash
run_python.bat 10
```

### Option 2: Direct Python Execution

```bash
export PYTHONPATH="${PYTHONPATH}:$(pwd)/src/python"
python -m xyz.jessyu.main 10
```

### Option 3: From Source Directory

```bash
cd src/python
python -m xyz.jessyu.main 10
```

## Command Line Arguments

The crawler accepts one argument: `SCROLL_COUNT`

```bash
python -m xyz.jessyu.main <SCROLL_COUNT>
```

- `SCROLL_COUNT`: Number of times to scroll down the page
- Example: `10` will scroll 10 times, crawling approximately 10-20 posts

## Output

The crawler will:

1. Open Chrome and navigate to Facebook
2. Scroll through the group page
3. Extract post content
4. Process each post using LLM
5. Store results in MongoDB (if configured)

Progress is logged to the console.

## Testing

Run the basic test suite:

```bash
python test_python.py
```

## Troubleshooting

### Import Errors

Make sure you've installed dependencies:
```bash
pip install -r requirements.txt
```

### Chrome Driver Issues

Ensure ChromeDriver matches your Chrome version:
```bash
# Check Chrome version
google-chrome --version  # Linux
chrome --version         # macOS

# Download matching ChromeDriver from:
# https://chromedriver.chromium.org/downloads
```

### Facebook Login Issues

1. Open Chrome manually
2. Navigate to Facebook and log in
3. Close Chrome
4. Run the crawler - it will use your saved session

### LLM Connection Issues

Ensure Ollama is running:
```bash
# Check if Ollama is running
curl http://localhost:11434/api/tags

# Start Ollama if not running
ollama serve
```

### MongoDB Connection Issues

If not using MongoDB, the crawler will still work but won't store data:
- Remove or comment out `DB_URL` in `.env`
- The crawler will log warnings but continue running

## Comparing with Java Version

Both implementations are functionally identical:

| Feature | Java | Python |
|---------|------|--------|
| Selenium Crawling | ✅ | ✅ |
| LLM Integration | ✅ | ✅ |
| MongoDB Storage | ✅ | ✅ |
| Multi-threading | ✅ | ✅ |
| Deduplication | ✅ | ✅ |

Choose based on your preference or existing tech stack!

## Next Steps

1. Customize the prompt in `src/main/resources/extract_prompt.txt`
2. Adjust settings in `src/python/xyz/jessyu/settings.py`
3. Modify LLM parameters in the settings
4. Add custom post-processing logic

## Support

For issues or questions:
- Check `TRANSLATION_NOTES.md` for implementation details
- Review the Java version for reference behavior
- Check the GitHub issues page

Happy crawling! 🕷️
