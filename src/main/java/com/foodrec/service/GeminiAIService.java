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

@Service
public class GeminiAIService extends AIService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public GeminiAIService(APIClient apiClient) {
        super(apiClient);
        this.model = "gemini-2.5-flash"; // Pastikan model ini aktif/valid
    }

    // === FITUR 1: ANALISA FOLLOW UP (Untuk Jurnal) ===
    @Override 
    public Map<String, String> analyzeCondition(String diseaseName, String userFeedback) {
        String prompt = "You are a medical assistant. A patient diagnosed with '" + diseaseName + "' " +
                "reported this follow-up condition: '" + userFeedback + "'.\n\n" +
                "Analyze if they need a doctor immediately or if they are recovering.\n" +
                "Return STRICT JSON ONLY (No Markdown):\n" +
                "{\n" +
                "  \"status\": \"RECOVERED\" (if getting better) OR \"DOCTOR_REQUIRED\" (if worse/critical) OR \"MONITORING\" (if neutral),\n" +
                "  \"message\": \"Your short advice (max 2 sentences)\"\n" +
                "}";

        try {
            String rawResponse = callAI(prompt);
            String cleanJson = cleanMarkdown(rawResponse);
            JsonObject json = JsonParser.parseString(cleanJson).getAsJsonObject();
            
            Map<String, String> result = new HashMap<>();
            // Ambil value dengan aman
            result.put("status", json.has("status") ? json.get("status").getAsString().toUpperCase() : "MONITORING");
            result.put("message", json.has("message") ? json.get("message").getAsString() : "Please consult a doctor.");
            return result;
        } catch (Exception e) {
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("status", "DOCTOR_REQUIRED");
            errorResult.put("message", "System cannot analyze. Consult a doctor.");
            return errorResult;
        }
    }

    // === FITUR 2: REKOMENDASI MAKANAN (Dashboard) ===
    @Override
    protected String buildPrompt(Disease disease) {
        String diseaseName = (disease.getName() != null) ? disease.getName() : "Unknown";
        
        return "You are a nutritionist AI. Patient diagnosis: " + diseaseName + ". " +
               "Provide dietary recommendations in STRICT JSON format. " +
               "NO Markdown, NO ```json wrappers. Just raw JSON.\n\n" +
               "JSON Structure:\n" +
               "{ \"foodsToEat\": [\"item1\", \"item2\", \"item3\"], " +
               "\"foodsToAvoid\": [\"item1\", \"item2\", \"item3\"], " +
               "\"additionalNotes\": \"Brief explanation\" }";
    }

    @Override
    protected String callAI(String prompt) throws Exception {
        // [FIX] URL ini sekarang bersih dari karakter markdown '[' atau ']'
        this.apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;
        
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

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = apiClient.sendPostRequest(this.apiUrl, requestBody.toString(), headers);
        
        // Error Handling API Google
        JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
        if (responseJson.has("error")) {
            throw new Exception("Google AI Error: " + responseJson.get("error").toString());
        }
        
        try {
            return responseJson.getAsJsonArray("candidates").get(0).getAsJsonObject()
                    .getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            throw new Exception("Empty response from AI");
        }
    }

    @Override
    protected FoodRecommendation parseResponse(String response, Disease disease) {
        FoodRecommendation recommendation = new FoodRecommendation(disease, getProviderName());
        try {
            String cleanJson = cleanMarkdown(response);
            JsonObject jsonResponse = JsonParser.parseString(cleanJson).getAsJsonObject();

            if (jsonResponse.has("foodsToEat")) {
                JsonArray arr = jsonResponse.getAsJsonArray("foodsToEat");
                for (int i = 0; i < arr.size(); i++) recommendation.addFoodToEat(arr.get(i).getAsString());
            }

            if (jsonResponse.has("foodsToAvoid")) {
                JsonArray arr = jsonResponse.getAsJsonArray("foodsToAvoid");
                for (int i = 0; i < arr.size(); i++) recommendation.addFoodToAvoid(arr.get(i).getAsString());
            }

            if (jsonResponse.has("additionalNotes")) {
                recommendation.setAdditionalNotes(jsonResponse.get("additionalNotes").getAsString());
            }
            
        } catch (Exception e) {
            recommendation.setAdditionalNotes("Raw AI Response (Format Error): " + response);
        }
        return recommendation;
    }

    @Override
    protected String getProviderName() { return "Google Gemini (" + this.model + ")"; }

    private String cleanMarkdown(String text) {
        if (text == null) return "";
        String cleaned = text.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
        return cleaned.trim();
    }
}