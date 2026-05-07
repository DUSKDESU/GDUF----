package test.openai.service.impl;

import test.openai.dto.request.CreateApiKeyRequest;
import test.openai.dto.response.ApiKeyListResponse;
import test.openai.dto.response.ApiKeyResponse;
import test.openai.entity.ApiKey;
import test.openai.entity.User;
import test.openai.repository.ApiKeyRepository;
import test.openai.repository.UserRepository;
import test.openai.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApiKeyServiceImpl implements ApiKeyService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Override
    public ApiKey createApiKey(Long userId, CreateApiKeyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String apiKey = generateApiKey();

        ApiKey apiKeyEntity = new ApiKey();
        apiKeyEntity.setApiKey(apiKey);
        apiKeyEntity.setUserId(userId);
        apiKeyEntity.setName(request.getName() != null ? request.getName() : "Default Key");
        apiKeyEntity.setRateLimit(request.getRateLimit());
        apiKeyEntity.setDailyLimit(request.getDailyLimit());
        apiKeyEntity.setExpiresAt(request.getExpiresAt());
        apiKeyEntity.setIsActive(true);

        return apiKeyRepository.save(apiKeyEntity);
    }

    @Override
    public List<ApiKeyListResponse> getUserApiKeys(Long userId) {
        List<ApiKey> apiKeys = apiKeyRepository.findByUserIdAndIsActiveTrue(userId);

        return apiKeys.stream().map(this::convertToApiKeyListResponse).collect(Collectors.toList());
    }

    @Override
    public ApiKeyResponse getApiKeyById(Long userId, Long apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .filter(key -> key.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("API密钥不存在或无权限访问"));

        return convertToApiKeyResponse(apiKey);
    }

    @Override
    public ApiKey findApiKeyByValue(String apiKeyValue) {
        return apiKeyRepository.findByApiKey(apiKeyValue)
                .filter(ApiKey::getIsActive)
                .orElse(null);
    }

    @Override
    public ApiKey updateApiKey(Long userId, Long apiKeyId, CreateApiKeyRequest request) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .filter(key -> key.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("API密钥不存在或无权限访问"));

        apiKey.setName(request.getName());
        apiKey.setRateLimit(request.getRateLimit());
        apiKey.setDailyLimit(request.getDailyLimit());
        apiKey.setExpiresAt(request.getExpiresAt());

        return apiKeyRepository.save(apiKey);
    }

    @Override
    public void deleteApiKey(Long userId, Long apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .filter(key -> key.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("API密钥不存在或无权限访问"));

        apiKeyRepository.delete(apiKey);
    }

    @Override
    public ApiKey toggleApiKeyStatus(Long userId, Long apiKeyId, Boolean active) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .filter(key -> key.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("API密钥不存在或无权限访问"));

        apiKey.setIsActive(active);
        return apiKeyRepository.save(apiKey);
    }

    @Override
    public boolean isApiKeyValid(String apiKeyValue) {
        return apiKeyRepository.existsByApiKeyAndIsActiveTrue(apiKeyValue);
    }

    @Override
    public void incrementApiKeyUsage(Long apiKeyId) {
        apiKeyRepository.incrementUsageCount(apiKeyId);
    }

    @Override
    public boolean checkApiKeyLimits(Long apiKeyId) {
        Optional<ApiKey> optionalApiKey = apiKeyRepository.findById(apiKeyId);
        if (optionalApiKey.isEmpty()) {
            return false;
        }

        ApiKey apiKey = optionalApiKey.get();

        if (apiKey.getDailyLimit() != null && apiKey.getUsedCount() >= apiKey.getDailyLimit()) {
            return false;
        }

        return true;
    }

    @Override
    public List<ApiKeyResponse> getAllApiKeys() {
        List<ApiKey> apiKeys = apiKeyRepository.findAll();
        return apiKeys.stream().map(this::convertToApiKeyResponse).collect(Collectors.toList());
    }

    private String generateApiKey() {
        StringBuilder sb = new StringBuilder("sk-");
        for (int i = 0; i < 48; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private ApiKeyListResponse convertToApiKeyListResponse(ApiKey apiKey) {
        ApiKeyListResponse response = new ApiKeyListResponse();
        response.setId(apiKey.getId());
        response.setMaskedApiKey(maskApiKey(apiKey.getApiKey()));
        response.setName(apiKey.getName());
        response.setIsActive(apiKey.getIsActive());
        response.setUsedCount(apiKey.getUsedCount());
        response.setDailyLimit(apiKey.getDailyLimit());
        response.setExpiresAt(apiKey.getExpiresAt());
        response.setCreatedAt(apiKey.getCreatedAt());
        response.setUpdatedAt(apiKey.getUpdatedAt());
        return response;
    }

    private ApiKeyResponse convertToApiKeyResponse(ApiKey apiKey) {
        ApiKeyResponse response = new ApiKeyResponse();
        response.setId(apiKey.getId());
        response.setApiKey(apiKey.getApiKey());
        response.setMaskedApiKey(maskApiKey(apiKey.getApiKey()));
        response.setName(apiKey.getName());
        response.setUserId(apiKey.getUserId());
        response.setIsActive(apiKey.getIsActive());
        response.setRateLimit(apiKey.getRateLimit());
        response.setDailyLimit(apiKey.getDailyLimit());
        response.setUsedCount(apiKey.getUsedCount());
        response.setExpiresAt(apiKey.getExpiresAt());
        response.setCreatedAt(apiKey.getCreatedAt());
        response.setUpdatedAt(apiKey.getUpdatedAt());
        return response;
    }

    private String maskApiKey(String fullApiKey) {
        if (fullApiKey == null || fullApiKey.length() < 8) {
            return "***";
        }
        return fullApiKey.substring(0, 3) + "***" +
                fullApiKey.substring(fullApiKey.length() - 4);
    }
}
