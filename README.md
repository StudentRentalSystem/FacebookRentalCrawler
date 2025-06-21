## 📘 Facebook Group Crawler

由 **Selenium** 驅動的 Facebook 社團貼文爬蟲，具備自動滾動、展開「See more」、解析與結構化貼文等功能，支援串接 MongoDB 儲存。開發者：**JessYu-1011**, **hding4915**

---

### 📦 功能特色

* ✅ 自動登入 Facebook 並導向指定社團
* 🔄 自動滾動頁面載入更多貼文
* 🔍 自動展開「See more」全文內容
* 🧠 搭配 `LLaMA3-8B-Instruct` 將貼文轉為統一 JSON 格式
* 🧩 使用 SHA-1 過濾重複貼文
* 💾 支援 MongoDB 儲存（儲存邏輯已預留，尚未完成）

---

### 🚀 快速開始

#### ✅ 環境需求

* Java 17+
* Gradle
* Chrome 瀏覽器與對應版本 chromedriver
* 已登入的 Facebook 帳號（使用本機帳號登入）
* MongoDB（可選）

#### 🛠 環境變數設定

| 變數名稱                 | 說明                                          |
|----------------------|---------------------------------------------|
| `FACEBOOK_GROUP_URL` | 欲爬取的 Facebook 社團網址                          |
| `DB_URL`             | MongoDB 連線字串（可選）                            |
| `CLIENT_TOKEN`       | GitHub Personal Access Token，用於抓取 LLMParser |
| `LLM_SERVER_ADDRESS` | Ollama Server Address                       |
| `LLM_SERVER_PORT`    | Ollama Server Port                          |


---

### 🧠 系統機制說明

* **貼文來源**：Facebook 租屋社團
* **模型處理**：透過 `llama3:8b` 將貼文自然語言轉為下列格式：

```json
{
  "地址": "市區路地址",
  "租金": {"maxRental": 5000 , "minRental": 8000},
  "坪數": [],
  "格局": {"房":0, "廳":0, "衛":0},
  "性別限制": {"男": 0, "女": 0},
  "是否可養寵物": -1,
  "是否可養魚": -1,
  "是否可開伙": -1,
  "是否有電梯": -1,
  "是否可租屋補助": -1,
  "是否有頂樓加蓋": -1,
  "是否有機車停車位": -1,
  "是否有汽車停車位": -1,
  "聯絡方式": [
    {
      "聯絡人": "name",
      "手機": ["手機號碼"],
      "lineID": ["line ID"],
      "lineLink": ["line 連結"],
      "others": ["其他聯絡方式"]
    }
  ],
  "照片": []
}
```

---

## 🗂️ 專案檔案結構

```
FacebookRentalCrawler/
├── .gradle/
├── .idea/
├── build/
├── gradle/
├── src/
│   └── main/
│       ├── java/xyz/jessyu/
│       │   ├── Crawler.java
│       │   ├── FetchAllIds.java
│       │   ├── Main.java
│       │   ├── ProcessPosts.java
│       │   ├── RentalExtractor.java
│       │   ├── Settings.java
│       │   ├── StoreToDB.java
│       │   └── Utils.java
│       └── resources/
│           ├── extract_prompt.txt
│           ├── extracted_data.json
│           ├── logback.xml
│           └── rental_posts.json
├── build.gradle.kts
├── gradlew
├── gradlew.bat
├── README.md
└── settings.gradle.kts
```

---

### ▶️ 執行方式

1. 安裝與執行 [Ollama](https://ollama.com/)，並拉取模型：

```bash
ollama pull llama3:8b
ollama pull nomic-embed-text
```

2. 編譯與執行：

```bash
./gradlew build
java -cp build/libs/facebook-crawler.jar xyz.jessyu.Main <SCROLL_COUNT>
```

範例：

```bash
java -cp build/libs/facebook-crawler.jar xyz.jessyu.Main 10
```

---

### 💾 MongoDB 儲存

`StoreToDB.java` 預留資料儲存模組，可延伸實作自動入庫邏輯。

---

### 📦 自動發佈（CI/CD）

可搭配 GitHub Actions，於 `main` 分支觸發建置與發佈流程：

* 編輯 `.github/workflows/gradle-publish.yml`
* 設定 `build.gradle.kts` 中的發佈設定

---

### 📝 授權條款

MIT License
