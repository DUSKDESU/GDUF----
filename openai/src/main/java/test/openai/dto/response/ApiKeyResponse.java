package test.openai.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiKeyResponse {

    private Long id;
    private String apiKey;  // 这里只返回密钥的部分内容
    private String maskedApiKey;
    private String name;
    private Long userId;
    private Boolean isActive;
    private Integer rateLimit;
    private Integer dailyLimit;
    private Integer usedCount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
