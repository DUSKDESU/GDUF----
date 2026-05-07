package test.openai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
//暂且无用
public class FileUploadRequest {
    @NotBlank(message = "文件用途不能为空")
    @Size(max = 50, message = "用途描述不能超过50个字符")
    private String purpose;
}
