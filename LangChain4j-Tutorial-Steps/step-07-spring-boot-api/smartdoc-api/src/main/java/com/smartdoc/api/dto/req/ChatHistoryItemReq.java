package com.smartdoc.api.dto.req;

public record ChatHistoryItemReq(
        String role,
        String content
) {}
