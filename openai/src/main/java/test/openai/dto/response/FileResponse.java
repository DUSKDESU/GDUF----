package test.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileResponse {
    
    private String id;
    private String object;
    private Long bytes;
    private LocalDateTime createdAt;
    private String filename;
    private String purpose;
    private String status;
    
    private List<FileResponse> data;
}
