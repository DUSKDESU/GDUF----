package test.openai.service;

import test.openai.dto.request.ChatCompletionRequest;
import test.openai.dto.response.ChatCompletionResponse;
import test.openai.entity.Completion;

import java.util.concurrent.CompletableFuture;

public interface CompletionService {

    CompletableFuture<ChatCompletionResponse> createChatCompletion(ChatCompletionRequest request, String apiKey);//创建聊天


    ChatCompletionResponse simulateOpenAIResponse(ChatCompletionRequest request);//虚拟回复

    boolean checkQuotaLimits(Long userId, Long apiKeyId);//检查用户配额限制

    void deleteCompletion(String completionId, Long userId);//删除生成结果

    Completion cancelCompletion(String completionId, Long userId);//取消生成结果

    Completion getCompletionById(String completionId, Long userId);//获取生成结果
}
