package com.smartdoc.api.dto;

public record ChatHistoryItem(
        String role,
        String content
) {}
