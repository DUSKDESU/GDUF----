package test.openai.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import test.openai.dto.request.ModelRequest;
import test.openai.entity.Model;
import test.openai.repository.ModelRepository;
import test.openai.service.ModelService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class ModelServiceImpl implements ModelService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;



    @Override
    @Transactional
    public void initializeDefaultModels() {
        log.info("开始初始化默认模型列表");
        
        String[] defaultModels = {
            "gpt-4o",
            "gpt-4-turbo",
            "gpt-4",
            "gpt-3.5-turbo",
            "qwen-plus",
            "qwen-turbo",
            "qwen-max"
        };
        
        int count = 0;
        for (String modelName : defaultModels) {
            Model existingModel = modelRepository.findByModelName(modelName);
            
            if (existingModel == null) {
                Model model = new Model();
                model.setModelName(modelName);
                model.setDisplayname(modelName.toUpperCase());
                model.setIsActive(true);
                model.setDescription("默认模型: " + modelName);
                model.setMaxTokens(1024);
                model.setSupportsStreaming(true);
                modelRepository.save(model);
                count++;
                log.info("新增默认模型: {}", modelName);
            }
        }
        
        log.info("默认模型初始化完成，共新增 {} 个模型", count);
    }

    @Override
    public List<Model> getActiveModels() {
        List<Model> allModels = modelRepository.findAll();
        return allModels.stream()
                .filter(model -> model.getIsActive())
                .toList();
    }



    public Model addModel(ModelRequest modelRequest, Long ownerId) {
        Model existingModel = modelRepository.findByModelName(modelRequest.getModelName());
        
        if (existingModel != null) {
            throw new RuntimeException("模型已存在: " + modelRequest.getModelName());
        }
        
        Model model = new Model();
        model.setModelName(modelRequest.getModelName());
        model.setDisplayname(modelRequest.getDisplayname());
        model.setDescription(modelRequest.getDescription());
        model.setMaxTokens(modelRequest.getMaxTokens() != null ? modelRequest.getMaxTokens() : 1024);
        model.setSupportsStreaming(modelRequest.getSupportsStreaming() != null ? modelRequest.getSupportsStreaming() : true);
        model.setOwnerid(modelRequest.getOwnerId() != null ? modelRequest.getOwnerId() : ownerId);
        model.setIsActive(true);
        
        Model savedModel = modelRepository.save(model);
        log.info("新增模型成功: {}, 所有者ID: {}", savedModel.getModelName(), savedModel.getOwnerid());
        
        return savedModel;
    }
}
