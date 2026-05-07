package test.openai.controller;

import test.openai.dto.response.ApiResponse;
import test.openai.dto.request.UserLoginRequest;
import test.openai.dto.request.UserRegisterRequest;
import test.openai.dto.response.JwtResponse;
import test.openai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public ResponseEntity<ApiResponse<JwtResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {
        try {
            // 注册用户
            var user = userService.register(request);

            UserLoginRequest loginRequest = new UserLoginRequest();
            loginRequest.setIdentifier(request.getUsername());
            loginRequest.setPassword(request.getPassword());

            JwtResponse response = userService.login(loginRequest);

            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("注册失败: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并获取JWT令牌")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody UserLoginRequest request) {
        try {
            JwtResponse response = userService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("登录失败: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@RequestParam String refreshToken) {
        try {
            // 实现刷新令牌逻辑
            // 这里添加具体的刷新令牌实现
            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("令牌刷新失败: " + e.getMessage()));
        }
    }
}
