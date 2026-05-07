package test.openai.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChatCompletionRequest {

    @NotEmpty(message = "消息不能为空")
    private List<Message> messages;

    @NotNull(message = "模型不能为空")
    private String model;

    private Double temperature = 1.0;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Boolean stream = false;

    private Double topP = 1.0;

    private Integer n = 1;

    private List<String> stop;

    @JsonProperty("presence_penalty")
    private Double presencePenalty = 0.0;

    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty = 0.0;

    @JsonProperty("logit_bias")
    private Map<String, Integer> logitBias;

    private String user;

    @Data
    public static class Message {
        @NotNull(message = "角色不能为空")
        private String role;
        @NotNull(message = "内容不能为空")
        private String content;
    }
}
