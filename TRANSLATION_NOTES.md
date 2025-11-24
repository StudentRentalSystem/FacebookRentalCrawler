# Java to Python Translation Mapping

## File Structure Mapping

| Java File | Python File | Status | Notes |
|-----------|-------------|--------|-------|
| `Settings.java` | `settings.py` | ✅ Complete | Configuration and environment management |
| `Utils.java` | `utils.py` | ✅ Complete | SHA-256 hashing and JSON parsing |
| `Crawler.java` | `crawler.py` | ✅ Complete | Selenium-based Facebook crawling |
| `FetchAllIds.java` | `fetch_all_ids.py` | ✅ Complete | MongoDB ID retrieval |
| `RentalExtractor.java` | `rental_extractor.py` | ✅ Complete | LLM-based data extraction |
| `ProcessPosts.java` | `process_posts.py` | ✅ Complete | Post processing logic |
| `StoreToDB.java` | `store_to_db.py` | ✅ Complete | MongoDB storage operations |
| `Main.java` | `main.py` | ✅ Complete | Entry point with threading |
| N/A | `llm_client.py` | ✅ New | LLM client implementation (replaces external library) |

## Key Implementation Differences

### 1. LLM Client
- **Java**: Uses external `io.github.studentrentalsystem.LLMClient` library
- **Python**: Custom `llm_client.py` that directly calls Ollama API via HTTP

### 2. Threading Model
- **Java**: Uses `ExecutorService` with fixed thread pool
- **Python**: Uses `ThreadPoolExecutor` (similar concept)

### 3. Queue Implementation
- **Java**: Uses `BlockingQueue<Post>` from `java.util.concurrent`
- **Python**: Uses `Queue<Post>` from `queue` module

### 4. WebDriver Setup
- **Java**: `ChromeDriver` with `ChromeOptions`
- **Python**: `webdriver.Chrome` with `Options` (same functionality)

### 5. MongoDB Driver
- **Java**: Uses `org.mongodb:mongodb-driver-sync`
- **Python**: Uses `pymongo` (official MongoDB driver)

### 6. Logging
- **Java**: Uses SLF4J with Logback
- **Python**: Uses built-in `logging` module

### 7. JSON Handling
- **Java**: Uses `org.json.JSONObject` and `org.bson.Document`
- **Python**: Uses built-in `json` module and Python dictionaries

## Features Parity

✅ All core features from Java version are implemented in Python:
- Facebook login and navigation
- Automatic scrolling and "See More" expansion
- Post deduplication using SHA-256 hashing
- LLM-based post parsing
- MongoDB integration
- Multi-threaded processing
- Retry logic for failed operations
- Configurable environment variables

## Additional Python Features

- **`.env` file support**: Added `.env.example` for easier configuration
- **Run scripts**: Created `run_python.sh` (Linux/macOS) and `run_python.bat` (Windows)
- **Lazy imports**: Package doesn't require all dependencies unless used
- **Simple installation**: Standard Python `requirements.txt` and `setup.py`

## Dependencies Comparison

### Java
```gradle
selenium-java:4.20.0
mongodb-driver-bom:5.4.0
json:20231013
llmdataparser:1.0.2 (custom library)
logback-classic:1.5.13
```

### Python
```
selenium==4.20.0
pymongo==4.8.0
requests==2.31.0
python-dotenv==1.0.0
```

## Running Instructions

### Java
```bash
./gradlew build
java -cp build/libs/facebook-crawler.jar xyz.jessyu.Main 10
```

### Python
```bash
pip install -r requirements.txt
python -m xyz.jessyu.main 10
# or
./run_python.sh 10
```

## Testing

- **Java**: Uses JUnit 5 (in src/test/java)
- **Python**: Created `test_python.py` for basic validation

## Notes

1. Both versions require:
   - Chrome browser with matching ChromeDriver
   - Ollama server running with llama3:8b model
   - Facebook account logged in via Chrome profile
   - MongoDB instance (optional)

2. Environment variables are identical for both versions
3. Output format and data structure are identical
4. Performance should be comparable between both versions
