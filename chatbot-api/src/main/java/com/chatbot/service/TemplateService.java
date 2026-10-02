package com.chatbot.service;

import com.chatbot.config.GeminiProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class TemplateService {

    private final GeminiProperties geminiProperties;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public TemplateService(GeminiProperties geminiProperties) {
        this.geminiProperties = geminiProperties;
    }

    public String loadActiveTemplate() {
        String name = geminiProperties.getTemplate();
        if (name == null || name.isBlank()) {
            name = "default";
        }
        String filename = name.endsWith(".txt") ? name : name + ".txt";
        try {
            Resource resource = resolver.getResource("classpath:templates/" + filename);
            if (!resource.exists()) {
                throw new IllegalStateException("Template not found: " + filename);
            }
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read template: " + filename, e);
        }
    }
}
