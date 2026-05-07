package test.openai.dto.response;

import lombok.Data;

@Data
public class JwtResponse {

    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
}
