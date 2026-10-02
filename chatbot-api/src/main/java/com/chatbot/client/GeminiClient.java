package com.chatbot.client;

import com.chatbot.config.GeminiProperties;
import com.chatbot.dto.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final GeminiProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    public GeminiClient(
            GeminiProperties properties,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
    }

    public String generate(String systemInstruction, List<ChatMessage> messages) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }

        String url = properties.getApiUrl()
                + "/" + properties.getModel()
                + ":generateContent";

        Map<String, Object> body = new HashMap<>();
        body.put("systemInstruction", Map.of(
                "parts", List.of(Map.of("text", systemInstruction))
        ));
        body.put("contents", toGeminiContents(messages));
        body.put("generationConfig", Map.of(
                "responseMimeType", "application/json"
        ));

        RestClientResponseException lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                String response = restClientBuilder.build()
                        .post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-goog-api-key", properties.getApiKey())
                        .body(body)
                        .retrieve()
                        .body(String.class);

                return extractText(response);
            } catch (RestClientResponseException e) {
                lastError = e;
                boolean retryable = e.getStatusCode().value() == 429 || e.getStatusCode().value() == 503;
                if (!retryable || attempt == 3) {
                    break;
                }
                try {
                    Thread.sleep(700L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new IllegalStateException(
                "Gemini API error: " + lastError.getStatusCode().value() + " - " + lastError.getResponseBodyAsString(),
                lastError
        );
    }

    private List<Map<String, Object>> toGeminiContents(List<ChatMessage> messages) {
        List<Map<String, Object>> contents = new ArrayList<>();

        if (messages == null || messages.isEmpty()) {
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", "Inicie a conversa."))
            ));
            return contents;
        }

        for (ChatMessage message : messages) {
            String role = "assistant".equalsIgnoreCase(message.getRole()) ? "model" : "user";
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", message.getContent() != null ? message.getContent() : ""))
            ));
        }
        return contents;
    }

    private String extractText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new IllegalStateException("Gemini returned an empty response");
            }
            return textNode.asText();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Gemini response", e);
        }
    }
}
