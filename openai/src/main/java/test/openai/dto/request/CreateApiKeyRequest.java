package test.openai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateApiKeyRequest {

    private String name;

    @NotNull(message = "每日限制不能为空")
    @Min(value = 1, message = "每日限制至少为1")
    @Max(value = 100000, message = "每日限制最大为100000")
    private Integer dailyLimit = 1000;

    @Min(value = 1, message = "速率限制至少为1")
    @Max(value = 1000, message = "速率限制最大为1000")
    private Integer rateLimit = 100;

    private LocalDateTime expiresAt;
}
