package test.openai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import test.openai.dto.request.ChatCompletionRequest;
import test.openai.dto.response.ChatCompletionResponse;
import test.openai.entity.Completion;
import test.openai.repository.CompletionRepository;
import test.openai.service.ApiKeyService;
import test.openai.service.CompletionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class CompletionServiceImpl implements CompletionService {

    @Autowired
    private CompletionRepository completionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiKeyService apiKeyService;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Override
    public CompletableFuture<ChatCompletionResponse> createChatCompletion(
            ChatCompletionRequest request, String apiKey) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                test.openai.entity.ApiKey apiKeyEntity = apiKeyService.findApiKeyByValue(apiKey);
                if (apiKeyEntity == null) {
                    throw new RuntimeException("无效的API密钥");
                }
                
                Completion completion = new Completion();
                completion.setCompletionId(UUID.randomUUID().toString());
                completion.setUserId(apiKeyEntity.getUserId());
                completion.setApiKeyId(apiKeyEntity.getId());
                completion.setModel(request.getModel() != null ? request.getModel() : "gpt-3.5-turbo");
                
                try {
                    completion.setRequestMessages(objectMapper.writeValueAsString(request.getMessages()));
                } catch (JsonProcessingException e) {
                    completion.setRequestMessages("[]");
                }
                
                completion.setStatus(Completion.StatusEnum.processing);
                completion.setTemperature(request.getTemperature() != null ? BigDecimal.valueOf(request.getTemperature()) : new BigDecimal("1.0"));
                completion.setStream(request.getStream() != null ? request.getStream() : false);
                completion.setStartedAt(LocalDateTime.now());
                
                completion = completionRepository.save(completion);
                
                long startTime = System.currentTimeMillis();
                ChatCompletionResponse response = callRealOpenAI(request);
                long processingTime = System.currentTimeMillis() - startTime;

                String content = "";
                if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                    ChatCompletionResponse.Message message = response.getChoices().get(0).getMessage();
                    if (message != null) {
                        content = message.getContent();
                    }
                }
                
                Integer usageTokens = 0;
                if (response.getUsage() != null) {
                    usageTokens = response.getUsage().getTotalTokens();
                }
                
                completion.setStatus(Completion.StatusEnum.completed);
                completion.setResponseContent(content);
                completion.setUsageTokens(usageTokens);
                completion.setFinishReason("stop");
                completion.setCompletedAt(LocalDateTime.now());
                completion.setProcessingTimeMs((int) processingTime);
                
                completion = completionRepository.save(completion);
                
                apiKeyService.incrementApiKeyUsage(apiKeyEntity.getId());
                
                return response;
                
            } catch (Exception e) {
                throw new RuntimeException("创建聊天失败: " + e.getMessage(), e);
            }
        });
    }




    private ChatCompletionResponse callRealOpenAI(ChatCompletionRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(openaiApiKey);
        
        HttpEntity<ChatCompletionRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    openaiApiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            String responseBody = rawResponse.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("API返回空响应");
            }
            
            if (responseBody.trim().startsWith("<") || responseBody.contains("<html")) {
                throw new RuntimeException("API返回HTML错误页面，请检查API地址和密钥是否正确。响应内容: " + responseBody.substring(0, Math.min(200, responseBody.length())));
            }
            
            return objectMapper.readValue(responseBody, ChatCompletionResponse.class);
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new RuntimeException("API请求失败: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            throw new RuntimeException("API服务器错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("调用API失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatCompletionResponse simulateOpenAIResponse(ChatCompletionRequest request) {
        ChatCompletionResponse response = new ChatCompletionResponse();
        response.setId("chatcmpl-" + UUID.randomUUID().toString().substring(0, 8));
        response.setObject("chat.completion");
        response.setModel(request.getModel() != null ? request.getModel() : "gpt-3.5-turbo");
        response.setCreated(System.currentTimeMillis() / 1000);
        
        ChatCompletionResponse.Message message = new ChatCompletionResponse.Message();
        message.setRole("assistant");
        message.setContent("这是一个模拟的 AI 回复。在实际部署中，这里会连接到真实的 OpenAI API 或其他大模型服务。");
        
        ChatCompletionResponse.Choice choice = new ChatCompletionResponse.Choice();
        choice.setIndex(0);
        choice.setMessage(message);
        choice.setFinishReason("stop");
        response.setChoices(java.util.Collections.singletonList(choice));
        
        ChatCompletionResponse.Usage usage = new ChatCompletionResponse.Usage();
        usage.setPromptTokens(20);
        usage.setCompletionTokens(30);
        usage.setTotalTokens(50);
        response.setUsage(usage);
        
        return response;
    }


    @Override
    public boolean checkQuotaLimits(Long userId, Long apiKeyId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        
        long todayCount = completionRepository.countByUserIdAndCreatedAtBetween(userId, startOfDay, now);
        
        if (todayCount >= 100) {
            return false;
        }
        
        return true;
    }

    @Override
    public void deleteCompletion(String completionId, Long userId) {
        Completion completion = completionRepository.findByCompletionId(completionId)
                .orElseThrow(() -> new RuntimeException("完成记录不存在: " + completionId));
        
        if (!completion.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此记录");
        }
        
        completionRepository.delete(completion);
    }

    @Override
    public Completion cancelCompletion(String completionId, Long userId) {
        Completion completion = completionRepository.findByCompletionId(completionId)
                .orElseThrow(() -> new RuntimeException("完成记录不存在: " + completionId));
        
        if (!completion.getUserId().equals(userId)) {
            throw new RuntimeException("无权取消此记录");
        }
        
        if (completion.getStatus() == Completion.StatusEnum.completed || 
            completion.getStatus() == Completion.StatusEnum.failed) {
            throw new RuntimeException("已完成或失败的记录无法取消");
        }
        
        completion.setStatus(Completion.StatusEnum.cancelled);
        completion.setCompletedAt(LocalDateTime.now());
        completion.setFinishReason("cancelled");
        
        return completionRepository.save(completion);
    }

    @Override
    public Completion getCompletionById(String completionId, Long userId) {
        Completion completion = completionRepository.findByCompletionId(completionId)
                .orElseThrow(() -> new RuntimeException("完成记录不存在: " + completionId));
        
        if (!completion.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此记录");
        }
        
        return completion;
    }
}