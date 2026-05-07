package test.openai.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import test.openai.dto.response.ApiResponse;
import test.openai.dto.request.CreateApiKeyRequest;
import test.openai.dto.response.ApiKeyListResponse;
import test.openai.dto.response.ApiKeyResponse;
import test.openai.entity.User;
import test.openai.repository.UserRepository;
import test.openai.service.ApiKeyService;

import java.util.List;

@RestController
@RequestMapping("/api/api-keys")
@Tag(name = "API密钥管理", description = "API密钥管理接口")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

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

    @PostMapping
    @Operation(summary = "创建API密钥", description = "为当前用户创建一个新的API密钥")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> createApiKey(
            Authentication authentication,
            @Valid @RequestBody CreateApiKeyRequest request) {
        try {
            Long userId = getCurrentUserId(authentication);
            var apiKey = apiKeyService.createApiKey(userId, request);
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

            return ResponseEntity.ok(ApiResponse.success("API密钥创建成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建API密钥失败: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "获取API密钥列表", description = "获取当前用户的所有API密钥")
    public ResponseEntity<ApiResponse<List<ApiKeyListResponse>>> getApiKeys(
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<ApiKeyListResponse> response = apiKeyService.getUserApiKeys(userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取API密钥列表失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{apiKeyId}")
    @Operation(summary = "获取API密钥详情", description = "获取指定ID的API密钥详情")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> getApiKey(
            Authentication authentication,
            @Parameter(description = "API密钥ID") @PathVariable Long apiKeyId) {
        try {
            Long userId = getCurrentUserId(authentication);
            ApiKeyResponse response = apiKeyService.getApiKeyById(userId, apiKeyId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取API密钥详情失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{apiKeyId}")
    @Operation(summary = "更新API密钥", description = "更新指定ID的API密钥信息")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> updateApiKey(
            Authentication authentication,
            @Parameter(description = "API密钥ID") @PathVariable Long apiKeyId, @Valid @RequestBody CreateApiKeyRequest request) {
        try {
            Long userId = getCurrentUserId(authentication);
            var updatedApiKey = apiKeyService.updateApiKey(userId, apiKeyId, request);
            ApiKeyResponse response = new ApiKeyResponse();
            response.setId(updatedApiKey.getId());
            response.setApiKey(updatedApiKey.getApiKey());
            response.setMaskedApiKey(updatedApiKey.getApiKey());
            response.setName(updatedApiKey.getName());
            response.setUserId(updatedApiKey.getUserId());
            response.setIsActive(updatedApiKey.getIsActive());
            response.setRateLimit(updatedApiKey.getRateLimit());
            response.setDailyLimit(updatedApiKey.getDailyLimit());
            response.setUsedCount(updatedApiKey.getUsedCount());
            response.setExpiresAt(updatedApiKey.getExpiresAt());
            response.setCreatedAt(updatedApiKey.getCreatedAt());
            response.setUpdatedAt(updatedApiKey.getUpdatedAt());

            return ResponseEntity.ok(ApiResponse.success("API密钥更新成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新API密钥失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{apiKeyId}")
    @Operation(summary = "删除API密钥", description = "删除指定ID的API密钥")
    public ResponseEntity<ApiResponse<Void>> deleteApiKey(
            Authentication authentication,
            @Parameter(description = "API密钥ID") @PathVariable Long apiKeyId) {
        try {
            Long userId = getCurrentUserId(authentication);
            apiKeyService.deleteApiKey(userId, apiKeyId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除API密钥失败: " + e.getMessage()));
        }
    }

    @PatchMapping("/{apiKeyId}/toggle-status")
    @Operation(summary = "切换API密钥状态", description = "激活或停用指定ID的API密钥")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> toggleApiKeyStatus(
            Authentication authentication,
            @Parameter(description = "API密钥ID") @PathVariable Long apiKeyId,
            @RequestParam Boolean active) {
        try {
            Long userId = getCurrentUserId(authentication);
            var toggledApiKey = apiKeyService.toggleApiKeyStatus(userId, apiKeyId, active);
            ApiKeyResponse response = new ApiKeyResponse();
            response.setId(toggledApiKey.getId());
            response.setApiKey(toggledApiKey.getApiKey());
            response.setMaskedApiKey(toggledApiKey.getApiKey());
            response.setName(toggledApiKey.getName());
            response.setUserId(toggledApiKey.getUserId());
            response.setIsActive(toggledApiKey.getIsActive());
            response.setRateLimit(toggledApiKey.getRateLimit());
            response.setDailyLimit(toggledApiKey.getDailyLimit());
            response.setUsedCount(toggledApiKey.getUsedCount());
            response.setExpiresAt(toggledApiKey.getExpiresAt());
            response.setCreatedAt(toggledApiKey.getCreatedAt());
            response.setUpdatedAt(toggledApiKey.getUpdatedAt());

            return ResponseEntity.ok(ApiResponse.success(
                    active ? "API密钥已激活" : "API密钥已停用", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("切换API密钥状态失败: " + e.getMessage()));
        }
    }
}
