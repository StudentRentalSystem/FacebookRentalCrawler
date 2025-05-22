📘 Facebook Group Crawler
由 Selenium 驅動的 Facebook Group 貼文爬蟲，可自動滾動頁面、展開「See more」並擷取貼文內容，支援 MongoDB 儲存。由 JessYu-1011 開發。

📦 功能特色
自動登入 Facebook 並進入指定社團

滾動指定次數以載入更多貼文

自動展開「See more」貼文

使用 SHA-1 判斷貼文是否重複

可串接 MongoDB 儲存資料（尚未實作完成）

🚀 快速開始
✅ 環境需求
Java 17+

Gradle

Chrome 瀏覽器

chromedriver

Facebook 帳號（已登入）

MongoDB

🛠 環境變數
需設定以下環境變數：

名稱	說明
FACEBOOK_GROUP_URL: 欲爬取的 Facebook 社團網址
DB_URL:	MongoDB 連線字串
CLIENT_TOKEN: GitHub Personal access token 用來抓取 LLMParser 

📁 專案結構
css
複製
編輯
.
├── main
│   ├── java
│   │   └── xyz
│   │       └── jessyu
│   │           ├── Crawler.java
│   │           ├── Main.java
│   │           ├── ProcessPosts.java
│   │           ├── Settings.java
│   │           └── StoreToDB.java
│   └── resources
│       └── logback.xml
└── test
└── java
└── DBTest.java

▶️ 執行方式
bash
複製
編輯
./gradlew build
java -cp build/libs/facebook-crawler.jar xyz.jessyu.Main <SCROLL_COUNT>
範例：

bash
複製
編輯
java -cp build/libs/facebook-crawler.jar xyz.jessyu.Main 10
🧪 執行流程
開啟 Chrome（使用本機帳號已登入 Facebook）

進入社團頁面

滾動指定次數

擷取貼文內容

💾 MongoDB 儲存
StoreToDB 類別預留了貼文處理與儲存功能


📦 發佈與 CI/CD
你可以使用 GitHub Actions + Gradle 自動發佈到 GitHub Packages：

推送到 main 即觸發建置與發佈流程（需設定 build.gradle 與 .github/workflows/publish.yml）

📝 授權
MIT License