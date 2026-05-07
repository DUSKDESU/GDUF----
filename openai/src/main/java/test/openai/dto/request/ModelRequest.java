package test.openai.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModelRequest {
    @NotBlank(message = "模型名不能为空")
    @Size(min = 3, max = 30, message = "模型长度必须在3-30个字符之间")
    private String modelName; 
    
    @NotBlank(message = "显示名称不能为空")
    @Size(min = 3, max = 30, message = "显示名称长度必须在3-30个字符之间")
    private String displayname;

    @NotBlank(message = "描述不能为空")
    @Size(min = 3, max = 200, message = "描述长度必须在3-200个字符之间")
    private String description;

    private Integer maxTokens;

    private Boolean supportsStreaming;

    private Long ownerId;

}
