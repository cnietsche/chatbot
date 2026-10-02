package com.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {

    private String role = "assistant";
    private String content = "";
    private List<String> options = new ArrayList<>();

    public ChatResponse() {
    }

    public ChatResponse(String content, List<String> options) {
        this.content = content;
        this.options = options != null ? options : new ArrayList<>();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? options : new ArrayList<>();
    }
}
