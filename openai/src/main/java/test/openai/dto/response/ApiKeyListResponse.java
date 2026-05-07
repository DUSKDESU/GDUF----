package test.openai.dto.response;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiKeyListResponse {

    private Long id;
    private String maskedApiKey;  // 只显示部分密钥内容
    private String name;
    private Boolean isActive;
    private Integer usedCount;
    private Integer dailyLimit;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
