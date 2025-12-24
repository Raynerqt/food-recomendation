package com.foodrec.util;

/**
 * APIClient Interface
 * Demonstrates: Interface, Contract Definition
 */
public interface APIClient {
    /**
     * Send HTTP POST request to API endpoint
     * @param url API endpoint URL
     * @param payload JSON payload
     * @param headers Request headers
     * @return Response body as String
     * @throws Exception if request fails
     */
    String sendPostRequest(String url, String payload, java.util.Map<String, String> headers) 
            throws Exception;
    
    /**
     * Get the last HTTP status code
     * @return HTTP status code
     */
    int getLastStatusCode();
    
    /**
     * Check if last request was successful
     * @return true if status code is 2xx
     */
    boolean isLastRequestSuccessful();
    
    /**
     * Set connection timeout
     * @param timeoutMs timeout in milliseconds
     */
    void setConnectionTimeout(int timeoutMs);
    
    /**
     * Set read timeout
     * @param timeoutMs timeout in milliseconds
     */
    void setReadTimeout(int timeoutMs);
}