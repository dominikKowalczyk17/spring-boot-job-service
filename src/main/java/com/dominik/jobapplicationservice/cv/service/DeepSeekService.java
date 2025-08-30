package com.dominik.jobapplicationservice.cv.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class DeepSeekService {

    @Value("${openrouter.api.key:}")
    private String apiKey;

    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public DeepSeekService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Data
    public static class DeepSeekRequest {
        private String model = "google/gemma-2-9b-it:free";
        private List<Message> messages;
        private double temperature = 0.7;
        @JsonProperty("max_tokens")
        private int maxTokens = 1000;
    }

    @Data
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    public static class DeepSeekResponse {
        private List<Choice> choices;

        @Data
        public static class Choice {
            private Message message;
        }
    }

    public String generateText(String prompt) {
        try {
            log.info("API Key status - null: {}, empty: {}, length: {}",
                    apiKey == null, apiKey.isEmpty(), apiKey != null ? apiKey.length() : 0);

            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("OpenRouter API key not configured, returning mock response");
                return "Mock AI Response: " + prompt.substring(0, Math.min(50, prompt.length())) + "... [MOCK - Configure OPENROUTER_API_KEY]";
            }

            // Create request
            DeepSeekRequest request = new DeepSeekRequest();
            request.setMessages(List.of(new Message("user", prompt)));

            log.info("Sending prompt to OpenRouter, length: {}, model: {}", prompt.length(), request.getModel());

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            // Make request
            HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<DeepSeekResponse> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, DeepSeekResponse.class);

            log.info("Raw API response status: {}, body: {}",
                    response.getStatusCode(),
                    response.getBody() != null ? "present" : "null");

            if (response.getBody() != null &&
                    response.getBody().getChoices() != null &&
                    !response.getBody().getChoices().isEmpty()) {
                String content = response.getBody().getChoices().get(0).getMessage().getContent();
                log.info("AI Response received, length: {}, content preview: {}",
                        content != null ? content.length() : 0,
                        content != null ? content.substring(0, Math.min(100, content.length())) : "null");
                return content;
            }

            log.warn("Empty or invalid response structure from OpenRouter API");
            return "Error: Empty response from OpenRouter";

        } catch (Exception e) {
            log.error("Error calling OpenRouter API: {}", e.getMessage(), e);
            return "Error generating response: " +  e.getMessage();
        }
    }

}