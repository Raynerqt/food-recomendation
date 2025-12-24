package com.foodrec.util;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * HTTPClient Implementation
 * Demonstrates: Interface Implementation, Encapsulation
 */
@Component
public class HTTPClient implements APIClient {
    private int lastStatusCode;
    private int connectionTimeout = 30000; // 30 seconds
    private int readTimeout = 30000; // 30 seconds
    
    @Override
    public String sendPostRequest(String url, String payload, Map<String, String> headers) 
            throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            
            // Set headers
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            
            // Set payload
            if (payload != null) {
                StringEntity entity = new StringEntity(payload);
                httpPost.setEntity(entity);
            }
            
            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                this.lastStatusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return responseBody;
            }
        } catch (Exception e) {
            throw new Exception("HTTP request failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int getLastStatusCode() {
        return lastStatusCode;
    }
    
    @Override
    public boolean isLastRequestSuccessful() {
        return lastStatusCode >= 200 && lastStatusCode < 300;
    }
    
    @Override
    public void setConnectionTimeout(int timeoutMs) {
        this.connectionTimeout = timeoutMs;
    }
    
    @Override
    public void setReadTimeout(int timeoutMs) {
        this.readTimeout = timeoutMs;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
}