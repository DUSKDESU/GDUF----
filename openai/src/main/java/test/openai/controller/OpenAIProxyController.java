package test.openai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import test.openai.dto.response.ApiResponse;
import test.openai.dto.request.ChatCompletionRequest;
import test.openai.dto.response.ChatCompletionResponse;
import test.openai.entity.ApiKey;
import test.openai.entity.Completion;
import test.openai.service.ApiKeyService;
import test.openai.service.CompletionService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/v1")
@Tag(name = "OpenAI API代理", description = "OpenAI API代理接口")
public class OpenAIProxyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private CompletionService completionService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @PostMapping("/chat/completions")
    @Operation(summary = "聊天完成", description = "代理OpenAI的chat/completions接口")
    public DeferredResult<ResponseEntity<ApiResponse<ChatCompletionResponse>>> chatCompletions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody ChatCompletionRequest chatRequest) {

        DeferredResult<ResponseEntity<ApiResponse<ChatCompletionResponse>>> deferredResult = new DeferredResult<>();

        try {
            String apiKeyValue = extractApiKeyFromHeader(authHeader);

            if (!apiKeyService.isApiKeyValid(apiKeyValue)) {
                deferredResult.setResult(ResponseEntity.status(401)
                        .body(ApiResponse.error("无效的API密钥")));
                return deferredResult;
            }

            ApiKey apiKey = apiKeyService.findApiKeyByValue(apiKeyValue);
            if (apiKey == null) {
                deferredResult.setResult(ResponseEntity.status(401)
                        .body(ApiResponse.error("API密钥验证失败")));
                return deferredResult;
            }

            if (!apiKeyService.checkApiKeyLimits(apiKey.getId())) {
                deferredResult.setResult(ResponseEntity.status(429)
                        .body(ApiResponse.error("API密钥使用次数已达上限")));
                return deferredResult;
            }

            if (!completionService.checkQuotaLimits(apiKey.getUserId(), apiKey.getId())) {
                deferredResult.setResult(ResponseEntity.status(429)
                        .body(ApiResponse.error("配额限制已达到")));
                return deferredResult;
            }

            CompletableFuture<ChatCompletionResponse> future = completionService.createChatCompletion(chatRequest, apiKeyValue);

            future.thenAccept(response ->
                    {deferredResult.setResult(ResponseEntity.ok(ApiResponse.success(response)));}
            ).exceptionally(ex -> {
                deferredResult.setResult(ResponseEntity.status(500)
                        .body(ApiResponse.error("处理请求时发生错误: " + ex.getMessage())));
                return null;
            });

        } catch (Exception e) {
            deferredResult.setErrorResult(ResponseEntity.status(500)
                    .body(ApiResponse.error("服务器内部错误: " + e.getMessage())));
        }

        return deferredResult;
    }

    @PostMapping("/completions")
    @Operation(summary = "文本完成", description = "代理OpenAI的completions接口")
    public DeferredResult<ResponseEntity<ApiResponse<ChatCompletionResponse>>> completions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody ChatCompletionRequest completionRequest) {

        return chatCompletions(authHeader, completionRequest);
    }

    @GetMapping("/models")
    @Operation(summary = "获取模型列表", description = "获取可用的模型列表")
    public ResponseEntity<ApiResponse<Object>> listModels(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            String apiKeyValue = extractApiKeyFromHeader(authHeader);

            if (!apiKeyService.isApiKeyValid(apiKeyValue)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("无效的API密钥"));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Object> response = restTemplate.exchange(
                    "https://dashscope.aliyuncs.com/compatible-mode/v1/models",
                    HttpMethod.GET,
                    entity,
                    Object.class
            );

            return ResponseEntity.ok(ApiResponse.success(response.getBody()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("获取模型列表失败: " + e.getMessage()));
        }
    }

    @GetMapping("/chat/completions/{completionId}")
    @Operation(summary = "获取生成结果", description = "获取某次生成的详细结果")
    public ResponseEntity<ApiResponse<Completion>> getCompletion(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String completionId) {
        try {
            String apiKeyValue = extractApiKeyFromHeader(authHeader);
            
            if (!apiKeyService.isApiKeyValid(apiKeyValue)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("无效的API密钥"));
            }
            
            ApiKey apiKey = apiKeyService.findApiKeyByValue(apiKeyValue);
            if (apiKey == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("API密钥验证失败"));
            }
            
            Completion completion = completionService.getCompletionById(completionId, apiKey.getUserId());
            return ResponseEntity.ok(ApiResponse.success(completion));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/chat/completions/{completionId}")
    @Operation(summary = "删除生成结果", description = "删除某次生成记录")
    public ResponseEntity<ApiResponse<Void>> deleteCompletion(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String completionId) {
        try {
            String apiKeyValue = extractApiKeyFromHeader(authHeader);
            
            if (!apiKeyService.isApiKeyValid(apiKeyValue)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("无效的API密钥"));
            }
            
            ApiKey apiKey = apiKeyService.findApiKeyByValue(apiKeyValue);
            if (apiKey == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("API密钥验证失败"));
            }
            
            completionService.deleteCompletion(completionId, apiKey.getUserId());
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/chat/completions/{completionId}")
    @Operation(summary = "取消生成请求", description = "取消正在进行的生成请求")
    public ResponseEntity<ApiResponse<Completion>> cancelCompletion(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String completionId) {
        try {
            String apiKeyValue = extractApiKeyFromHeader(authHeader);
            
            if (!apiKeyService.isApiKeyValid(apiKeyValue)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("无效的API密钥"));
            }
            
            ApiKey apiKey = apiKeyService.findApiKeyByValue(apiKeyValue);
            if (apiKey == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("API密钥验证失败"));
            }
            
            Completion completion = completionService.cancelCompletion(completionId, apiKey.getUserId());
            return ResponseEntity.ok(ApiResponse.success("取消成功", completion));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private String extractApiKeyFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("无效的Authorization头");
    }
}
