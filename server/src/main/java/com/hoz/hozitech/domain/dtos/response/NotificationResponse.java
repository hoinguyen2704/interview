package com.hoz.hozitech.domain.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String title;
    private String content;
    private Boolean isRead;
    private String type;
    private String eventCode;
    private UUID orderId;
    private String targetUrl;
    private String targetType;
    private String targetId;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
