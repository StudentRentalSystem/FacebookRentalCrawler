package xyz.jessyu;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.github.studentrentalsystem.LLMClient;
import io.github.studentrentalsystem.LLMConfig;
import io.github.studentrentalsystem.ModelRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static io.github.studentrentalsystem.Utils.getStringJSON;


/**
 * <p>
 * This class is used to parse rental posts to structural json data <br>
 * @author hding4915
 */
public class RentalExtractor {
    private final int maxErrorTimes;
    private static String promptTemplate;
    private final LLMClient llmClient;
    private final String promptPath;


    /**
     * <p>
     * Default maxErrorTimes will be set to 5
     * Default llmConfig will use the default value from {@link LLMConfig}
     * @throws IOException if it gets error when reading the prompt file
     */
    public RentalExtractor() throws IOException{
        this(5, new LLMConfig(), Settings.getPromptPath());
    }

    public RentalExtractor(int maxErrorTimes, LLMConfig llmConfig) throws IOException{
        this(maxErrorTimes, llmConfig, Settings.getPromptPath());
    }


    /**
     *
     * @param maxErrorTimes set the maxErrorTimes for parsing the rental posts
     * @param llmConfig set the llm parameters
     * @throws IOException if it gets error when reading the prompt file
     */
    public RentalExtractor(int maxErrorTimes, LLMConfig llmConfig, String promptPath) throws IOException{
        this.maxErrorTimes = maxErrorTimes;
        this.promptPath = promptPath;
        parseResData();
        llmClient = new LLMClient(llmConfig);
    }


    private void parseResData() throws IOException {
        InputStream in = RentalExtractor.class.getClassLoader().getResourceAsStream(promptPath);
        promptTemplate = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IllegalArgumentException {
        String prompt = "請根據以下租屋需求文字，幫我轉換為 JSON 格式的 MongoDB 搜尋語句。請用繁體中文回答。\n" +
                "❗重要規則：只有在輸入文字明確提到的資訊，才需要加入搜尋條件。不要亂填預設值或空物件。\n" +
                "\n" +
                "可使用的欄位如下：\n" +
                "\n" +
                "- \"地址\": 若使用者輸入的內容提及地址地名（如某某市、某某區、某某路、某某巷...等地名結構），請將地名依照層級拆解為多個區段（如台南市東區勝利路應拆為「台南」、「東區」、「勝利路」），並針對每個部分個別使用 $regex 查詢，並加上 \"$options\": \"i\" 做模糊比對，請用 \"$and\" 連結。每個地址區段請各自生成一筆查詢語句，不要用 \"地址.台南\"、\"地址.東區\" 去查詢，\n" +
                "使用 {\"地址\": { \"$regex\": \"台南\", \"$options\": \"i\" }} 方式做查詢\n" +
                "使用者輸入可能包含完整或簡略的地點名稱（例如「台南市」、「台南」），請將這些同義地名視為相同，並用最小單位（例如「台南」）進行 MongoDB 查詢。\n" +
                "若有「勝利路」這種具體路名，請同時加入模糊匹配。\n" +
                "若使用者沒有提及地址相關資訊，請不要加入匹配\n" +
                "\n" +
                "- \"租金\": 為單一物件，查詢方式如 {\"租金.minRental\":3000,\"租金.maxRental\":5000}。若提及租金區間，請用 \"租金.minRental\", \"租金.maxRental\" 對 minRental 與 maxRental 進行查詢，請不要用 \"$elemMatch\" 搜尋。\n" +
                "若使用者要求一個大概的租金數值，像是: 我要租金大概在 5000 元，那麼請將 minRental, maxRental 設置為 5000 上下 1500 元\n" +
                "若使用者表明他要特定範圍的租金，像是: 我要租金 3000 ~ 6000 元，那麼搜尋方式為 {\"租金.minRental\":3000,\"租金.maxRental\":6000}\n" +
                "\n" +
                "- \"坪數\": 格式為 float 陣列，若使用者提及坪數，用 \"$elemMatch\" 搜尋\n" +
                "- \"格局\"：為物件包含 \"房\"、\"廳\"、\"衛\"，請使用 \"格局.房\"、\"格局.廳\"、\"格局.衛\" 方式進行比對。\n" +
                "- \"性別限制\": 為物件包含 \"男\"、\"女\"，數值為 int，請使用 \"性別限制.男\"、\"性別限制.女\" 方式進行比對\n" +
                "- \"是否可養寵物\"：為 int 數字格式 1 或是 0\n" +
                "- \"是否可養魚\"：為 int 數字格式 1 或是 0\n" +
                "- \"是否可開伙\"：為 int 數字格式 1 或是 0\n" +
                "- \"是否有電梯\"：為 int 數字格式 1 或是 0\n" +
                "- \"是否可租屋補助\": 為 int 數字格式 1 或是 0\n" +
                "- \"是否有頂樓加蓋\": 為 int 數字格式 1 或是 0\n" +
                "- \"是否有機車停車位\": 為 int 數字格式 1 或是 0\n" +
                "- \"是否有汽車停車位\": 為 int 數字格式 1 或是 0\n" +
                "\n" +
                "若使用者輸入提及「套房」、「雅房」，請視為 {\"格局.房\": 1, \"格局.廳\": 0, \"格局.衛\": 0}，並分別寫出三個條件。\n" +
                "\n" +
                "範例輸入: 電梯套房\n" +
                "範例輸出:\n" +
                "{\n" +
                "    \"$and\": [\n" +
                "        { \"格局.房\": 1 },\n" +
                "        { \"格局.廳\": 0 },\n" +
                "        { \"格局.衛\": 0 },\n" +
                "        { \"是否有電梯\": 1 }\n" +
                "    ]\n" +
                "}\n" +
                "\n" +
                "範例輸入: 我要租金在 3000 到 10000 元的房子\n" +
                "範例輸出:\n" +
                "{\n" +
                "  \"租金.minRental\": { \"$gte\": 3000 },\n" +
                "  \"租金.maxRental\": { \"$lte\": 10000 }\n" +
                "}\n" +
                "\n" +
                "範例輸入: 我要租金大概在 5000 元\n" +
                "範例輸出:\n" +
                "{\n" +
                "  \"租金.minRental\": { \"$gte\": 3500 },\n" +
                "  \"租金.maxRental\": { \"$lte\": 6500 }\n" +
                "}\n" +
                "\n" +
                "範例輸入: 我要男生女生都可以住的房子\n" +
                "範例輸出:\n" +
                "{\n" +
                "  \"性別限制.男\": 1,\n" +
                "  \"性別限制.女\": 1\n" +
                "}\n" +
                "\n" +
                "\n" +
                "input:{query}";

        LLMConfig llmConfig = new LLMConfig(
                LLMConfig.LLMMode.CHAT,
                "http://140.116.110.134",
                11434,
                "deepseek-r1:14b",
                false,
                null
        );
        LLMClient llmClient = new LLMClient(llmConfig);
        Scanner scanner = new Scanner(System.in);

        System.out.println(ModelRegistry.getAllModels());

        while (true) {
            System.out.print("輸入: ");
            String text = scanner.nextLine();

            String response = llmClient.callLocalModel(prompt.replace("{query}", text));
            System.out.println(response);
            response = llmClient.getDetailMessage(response);
            String output = response.replaceAll("(?s)<think>.*?</think>", "").trim();
            System.out.println(getStringJSON(output));
        }

        // Usage for stream mode = true

//        BlockingQueue<LLMClient.StreamData> queue = new LinkedBlockingQueue<>();
//
//        LLMConfig llmConfig1 = new LLMConfig(true, "http://localhost", 11434, LLMConfig.ModelType.MISTRAL, true, queue);
//
//        LLMClient llmClient = new LLMClient(llmConfig1);
//
//        new Thread(() -> {
//            String dummyPrompt = "what is the ollama model";
//            llmClient.callLocalModel(dummyPrompt);
//        }).start();
//
//
//        // Main thread to listen token
//        new Thread(() -> {
//            StringBuilder contentSoFar = new StringBuilder();
//
//            while (true) {
//                try {
//                    LLMClient.StreamData data = queue.take(); // blocking until the data exists
//                    if (data.token != null) {
//                        System.out.print(data.token);
//                        contentSoFar.append(data.token);
//                    }
//
//                    if (data.completed) {
//                        System.out.println("\n✅ 回覆完成");
//                        break;
//                    }
//                } catch (InterruptedException e) {
//                    break;
//                }
//            }
//        }).start();

    }

    /**
     * <p>
     * This function will call local LLM model to generate structural json data
     * @param post the rental post
     * @return type of {@link JSONObject} structural post
     */
    public JSONObject getJSONPost(String post) throws JSONException{
        String response = callLocalModel(post);
        JSONObject jsonObject = getStringJSON(response);
        jsonObject.put("坪數", correctRentalSize(jsonObject.getJSONArray("坪數")));
        return jsonObject;
    }

    private JSONObject noErrorJSONPost(String post) {
        String response  = callLocalModel(post);
        try {
            JSONObject jsonObject = getStringJSON(response);
            jsonObject.put("坪數", correctRentalSize(jsonObject.getJSONArray("坪數")));
            return jsonObject;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkFirstObject(JSONArray jsonArray) {
        return (jsonArray.length() == 1 && (jsonArray.getString(0).isEmpty() || jsonArray.getString(0).equals("未知")));
    }

    private void writeDefaultEmpty(JSONObject jsonObject, String key, Object value) {
        if (!jsonObject.has(key)) jsonObject.put(key, value);
    }

    /**
     * <p>
     * This function is used to adjust the structural json data to a fixed format
     * @param structural_post one rental post json object
     */
    public void formSameJSON(JSONObject structural_post) {
        JSONArray contact = structural_post.getJSONArray("聯絡方式");
        JSONArray result = new JSONArray();
        for (int i = 0; i < contact.length(); i++) {
            JSONObject personal_contact = contact.getJSONObject(i);
            writeDefaultEmpty(personal_contact, "聯絡人", "");
            writeDefaultEmpty(personal_contact, "手機", new JSONArray());
            writeDefaultEmpty(personal_contact, "lineID", new JSONArray());
            writeDefaultEmpty(personal_contact, "lineLink", new JSONArray());
            writeDefaultEmpty(personal_contact, "others", new JSONArray());

            String name = personal_contact.getString("聯絡人");
            JSONArray phone_nums = personal_contact.getJSONArray("手機");
            JSONArray lineID = personal_contact.getJSONArray("lineID");
            JSONArray lineLink = personal_contact.getJSONArray("lineLink");
            JSONArray others = personal_contact.getJSONArray("others");

            if (name.equals("未知")) name = "";
            if (checkFirstObject(phone_nums)) {
                phone_nums = new JSONArray();
            } else {
                JSONArray modified = new JSONArray();
                for (int j = 0; j < phone_nums.length(); j++) {
                    String phone = phone_nums.getString(j);
                    if (phone.contains("-")) modified.put(phone.replace("-", ""));
                    else modified.put(phone);
                }
                phone_nums = modified;
            }
            if (checkFirstObject(lineID)) lineID = new JSONArray();
            if (checkFirstObject(lineLink)) lineLink = new JSONArray();
            if (checkFirstObject(others)) others = new JSONArray();
            personal_contact = new JSONObject();
            personal_contact.put("聯絡人", name);
            personal_contact.put("手機", phone_nums);
            personal_contact.put("lineID", lineID);
            personal_contact.put("lineLink", lineLink);
            personal_contact.put("others", others);
            result.put(personal_contact);
        }
        structural_post.put("聯絡方式", result);
    }


    /**
     * <p>
     * This function is used to parse single rental post to structural json data. <br>
     * When getting errors, it will parse again until the error times >= maxErrorTimes.
     * @param post single rental post string
     * @return structural json post, type of {@link JSONObject}
     */
    public JSONObject getJSONPostNoError(String post) {
        JSONObject jsonPost;
        int errorTimes = 0;
        while ((jsonPost = noErrorJSONPost(post)) == null && errorTimes < maxErrorTimes) {
            errorTimes++;
        }
        if (jsonPost != null) formSameJSON(jsonPost);
        return jsonPost;
    }


    private JSONArray correctRentalSize(JSONArray rentalsSize) {
        JSONArray revisedRental = new JSONArray();
        for (int j = 0; j < rentalsSize.length(); j++) {
            if (rentalsSize.getFloat(j) >= 100) {
                revisedRental.put(-1);
            } else {
                revisedRental.put(rentalsSize.getFloat(j));
            }
        }
        return revisedRental;
    }


    /**
     * <p>
     * This function is used to parse multiple rental posts to structural json data
     * @param inputPath input file path of the original rental posts, is a .json file that contains ["post1", "post2", ...]
     * @param outputPath output file path of the structural json data, is a .json file
     */
    public void processPosts(String inputPath, String outputPath) {
        try {
            // Read all posts from input JSON file
            InputStream in = RentalExtractor.class.getClassLoader().getResourceAsStream(inputPath);
            String inputJson = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            JSONArray posts = new JSONArray(inputJson);
            List<JSONObject> results = new ArrayList<>();

            for (int i = 0; i < posts.length(); i++) {
                String post = posts.getString(i);
                System.out.println("⏳ 分析第 " + (i + 1) + " 筆貼文...");

                JSONObject parsed = getJSONPostNoError(post);

                results.add(parsed);
            }

            // Write results to output JSON file
            JSONArray outputJson = new JSONArray(results);
            Files.write(Paths.get(outputPath), outputJson.toString(2).getBytes(StandardCharsets.UTF_8));
            System.out.println("✅ 資料已輸出至 " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * This function is used to get the LLM response when converting the rental post to structural json data.
     * @param text the rental post
     * @return type of {@link String} response
     */
    public String callLocalModel(String text) {
        String prompt = promptTemplate.replace("{text}", text);

        String response = llmClient.callLocalModel(prompt);

        return llmClient.getDetailMessage(response);
    }
}
