package test.openai.service;



import test.openai.dto.request.CreateApiKeyRequest;
import test.openai.dto.response.ApiKeyListResponse;
import test.openai.dto.response.ApiKeyResponse;
import test.openai.entity.ApiKey;

import java.util.List;

public interface ApiKeyService {

    ApiKey createApiKey(Long userId, CreateApiKeyRequest request);//创建API密钥

    List<ApiKeyListResponse> getUserApiKeys(Long userId);//获取用户API密钥


    ApiKeyResponse getApiKeyById(Long userId, Long apiKeyId);//获取API密钥

    ApiKey findApiKeyByValue(String apiKeyValue);//通过密钥值获取API密钥


    ApiKey updateApiKey(Long userId, Long apiKeyId, CreateApiKeyRequest request);//更新API密钥


    void deleteApiKey(Long userId, Long apiKeyId);//删除API密钥

    ApiKey toggleApiKeyStatus(Long userId, Long apiKeyId, Boolean active);//激活或停用API密钥


    boolean isApiKeyValid(String apiKeyValue);//验证API密钥是否有效

    void incrementApiKeyUsage(Long apiKeyId);//递增API密钥使用次数


    boolean checkApiKeyLimits(Long apiKeyId);//检查API密钥使用限制

    List<ApiKeyResponse> getAllApiKeys();//获取所有API密钥
}
