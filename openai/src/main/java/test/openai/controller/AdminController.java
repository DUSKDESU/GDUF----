package test.openai.controller;


import test.openai.dto.response.ApiResponse;
import test.openai.dto.request.CreateApiKeyRequest;
import test.openai.dto.request.ModelRequest;
import test.openai.dto.response.ApiKeyResponse;
import test.openai.dto.response.UserResponse;
import test.openai.entity.ApiKey;
import test.openai.entity.Model;
import test.openai.entity.User;
import test.openai.service.ApiKeyService;
import test.openai.service.ModelService;
import test.openai.service.UserService;
import test.openai.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员管理", description = "管理员专用接口")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未认证用户");
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        return user.getId();
    }

    @GetMapping("/users")
    @Operation(summary = "获取所有用户", description = "管理员获取所有用户列表")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }

    @PostMapping("/users")
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserResponse request) {
        try {
            UserResponse user = userService.createUser(request);
            return ResponseEntity.ok(ApiResponse.success("用户创建成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建用户失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除用户", description = "管理员删除用户")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("用户删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除用户失败: " + e.getMessage()));
        }
    }

    @GetMapping("/api-keys")
    @Operation(summary = "获取所有API密钥", description = "管理员获取所有API密钥列表")
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> getAllApiKeys() {
        try {
            List<ApiKeyResponse> apiKeys = apiKeyService.getAllApiKeys();
            return ResponseEntity.ok(ApiResponse.success(apiKeys));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取API密钥列表失败: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/api-keys")
    @Operation(summary = "为用户分配API密钥", description = "管理员为用户创建并分配API密钥")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> assignApiKey(
            @PathVariable Long userId,
            @RequestBody CreateApiKeyRequest request) {
        try {
            ApiKey apiKey = apiKeyService.createApiKey(userId, request);
            ApiKeyResponse response = new ApiKeyResponse();
            response.setId(apiKey.getId());
            response.setApiKey(apiKey.getApiKey());
            response.setMaskedApiKey(apiKey.getApiKey());
            response.setName(apiKey.getName());
            response.setUserId(apiKey.getUserId());
            response.setIsActive(apiKey.getIsActive());
            response.setRateLimit(apiKey.getRateLimit());
            response.setDailyLimit(apiKey.getDailyLimit());
            response.setUsedCount(apiKey.getUsedCount());
            response.setExpiresAt(apiKey.getExpiresAt());
            response.setCreatedAt(apiKey.getCreatedAt());
            response.setUpdatedAt(apiKey.getUpdatedAt());

            return ResponseEntity.ok(ApiResponse.success("API密钥分配成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("分配API密钥失败: " + e.getMessage()));
        }
    }

    @PatchMapping("/users/{userId}/toggle-status")
    @Operation(summary = "切换用户状态", description = "管理员激活或停用用户")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable Long userId,
            @RequestParam Boolean active) {
        try {
            var user = userService.updateUserStatus(userId, active);
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setIsActive(user.getIsActive());
            response.setCreatedAt(user.getCreatedAt());
            response.setUpdatedAt(user.getUpdatedAt());

            return ResponseEntity.ok(ApiResponse.success(
                    active ? "用户已激活" : "用户已停用", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("切换用户状态失败: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{userId}/usage")
    @Operation(summary = "查看用户用量", description = "查看指定用户的使用情况")
    public ResponseEntity<ApiResponse<Object>> getUserUsage(@PathVariable Long userId) {
        try {
            var usage = userService.getUserUsageStats(userId);
            return ResponseEntity.ok(ApiResponse.success(usage));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户用量失败: " + e.getMessage()));
        }
    }

    @PostMapping("/models")
    @Operation(summary = "添加模型", description = "管理员添加新的AI模型到系统")
    public ResponseEntity<ApiResponse<Model>> addModel(Authentication authentication,
            @Valid @RequestBody ModelRequest modelRequest) {
        try {
            Long adminId = getCurrentUserId(authentication);
            Model model = modelService.addModel(modelRequest, adminId);
            return ResponseEntity.ok(ApiResponse.success("模型添加成功", model));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("添加模型失败: " + e.getMessage()));
        }
    }
}
