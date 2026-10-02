package com.chatbot.service;

import com.chatbot.client.GeminiClient;
import com.chatbot.dto.AiReply;
import com.chatbot.dto.ChatMessage;
import com.chatbot.dto.ChatRequest;
import com.chatbot.dto.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final String JSON_CONTRACT = """

            ---
            Formato de resposta obrigatório:
            Responda SEMPRE com um único JSON válido, sem markdown e sem texto fora do JSON, no formato:
            {"content":"texto da mensagem ao usuário","options":[]}

            - "content": mensagem em português para o usuário.
            - "options": lista de strings com opções clicáveis quando fizer sentido; caso contrário, [].
            """;

    private final TemplateService templateService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public ChatService(
            TemplateService templateService,
            GeminiClient geminiClient,
            ObjectMapper objectMapper
    ) {
        this.templateService = templateService;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public ChatResponse chat(ChatRequest request) {
        String systemPrompt = templateService.loadActiveTemplate() + JSON_CONTRACT;
        String raw = geminiClient.generate(systemPrompt, request.getMessages());
        AiReply reply = parseReply(raw);
        return new ChatResponse(reply.getContent(), reply.getOptions());
    }

    private AiReply parseReply(String raw) {
        try {
            String cleaned = raw.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
                cleaned = cleaned.replaceFirst("\\s*```$", "");
            }
            return objectMapper.readValue(cleaned, AiReply.class);
        } catch (Exception e) {
            AiReply fallback = new AiReply();
            fallback.setContent(raw);
            return fallback;
        }
    }
}
