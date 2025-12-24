package com.foodrec.service;

import com.foodrec.model.Disease;
import com.foodrec.model.FoodRecommendation;
import com.foodrec.util.APIClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("geminiAIService")
public class GeminiAIService extends AIService {

    @Value("${gemini.api.key}") // Pastikan key ada di application.properties, jangan hardcode di sini!
    private String geminiApiKey;

    public GeminiAIService(APIClient apiClient) {
        super(apiClient);
        // Ganti ke model yang lebih baru dan cepat
        this.model = "gemini-1.5-flash";
    }

    @Override
    protected String buildPrompt(Disease disease) {
        // Kita tambahkan instruksi agar Gemini tidak menggunakan Markdown
        return String.format(
            "You are a nutritionist AI assistant. A patient has been diagnosed with %s (%s). " +
            "Please provide:\n" +
            "1. A list of foods they SHOULD eat (minimum 5 items)\n" +
            "2. A list of foods they SHOULD AVOID (minimum 5 items)\n" +
            "3. Brief explanation for each recommendation\n" +
            "4. Additional dietary notes\n\n" +
            "IMPORTANT: Return ONLY raw JSON without Markdown formatting (no ```json ... ``` wrapper). " +
            "Structure: { 'foodsToEat': [], 'foodsToAvoid': [], 'additionalNotes': '' }",
            disease.getName(),
            disease.getCategory()
        );
    }

    @Override
    protected String callAI(String prompt) throws Exception {
        // 1. URL Construction
        this.apiUrl = String.format(
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent",
            this.model,
            geminiApiKey
        );

        // 2. Build Request Body
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Opsi tambahan: Mengatur strict JSON response (Hanya untuk model gemini-1.5 ke atas)
        // JsonObject generationConfig = new JsonObject();
        // generationConfig.addProperty("response_mime_type", "application/json");
        // requestBody.add("generationConfig", generationConfig);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-goog-api-key", geminiApiKey);

        // 3. Call API
        String response = apiClient.sendPostRequest(
            this.apiUrl,
            requestBody.toString(),
            headers
        );

        if (!apiClient.isLastRequestSuccessful()) {
            throw new Exception("Gemini API Error: " + apiClient.getLastStatusCode() + " - " + response);
        }

        // 4. Extract Text
        JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
        JsonArray candidates = responseJson.getAsJsonArray("candidates");

        if (candidates != null && candidates.size() > 0) {
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject contentObj = candidate.getAsJsonObject("content");
            JsonArray partsArray = contentObj.getAsJsonArray("parts");

            if (partsArray != null && partsArray.size() > 0) {
                return partsArray.get(0).getAsJsonObject().get("text").getAsString();
            }
        }

        throw new Exception("No content in Gemini response");
    }

    @Override
    protected FoodRecommendation parseResponse(String response, Disease disease) {
        FoodRecommendation recommendation = new FoodRecommendation(disease, getProviderName());

        try {
            // STEP PENTING: Bersihkan Markdown blocks jika AI bandel memberikannya
            String cleanJson = cleanMarkdown(response);

            JsonObject jsonResponse = JsonParser.parseString(cleanJson).getAsJsonObject();

            if (jsonResponse.has("foodsToEat")) {
                JsonArray foodsToEat = jsonResponse.getAsJsonArray("foodsToEat");
                for (int i = 0; i < foodsToEat.size(); i++) {
                    recommendation.addFoodToEat(foodsToEat.get(i).getAsString());
                }
            }

            if (jsonResponse.has("foodsToAvoid")) {
                JsonArray foodsToAvoid = jsonResponse.getAsJsonArray("foodsToAvoid");
                for (int i = 0; i < foodsToAvoid.size(); i++) {
                    recommendation.addFoodToAvoid(foodsToAvoid.get(i).getAsString());
                }
            }

            if (jsonResponse.has("additionalNotes")) {
                recommendation.setAdditionalNotes(jsonResponse.get("additionalNotes").getAsString());
            }

        } catch (Exception e) {
            System.err.println("JSON Parse Error: " + e.getMessage());
            // Fallback: simpan raw text jika gagal parsing JSON
            recommendation.setRecommendations(response); 
        }

        return recommendation;
    }

    @Override
    protected String getProviderName() {
        return "Google Gemini (" + this.model + ")";
    }

    // Helper untuk membersihkan ```json ... ```
    private String cleanMarkdown(String text) {
        if (text == null) return "";
        // Hapus ```json di awal dan ``` di akhir
        String cleaned = text.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}