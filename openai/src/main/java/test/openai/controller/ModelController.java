package test.openai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import test.openai.dto.response.ApiResponse;
import test.openai.entity.Model;
import test.openai.service.ModelService;

import java.util.List;

@RestController
@RequestMapping("/model")
@Tag(name = "模型管理", description = "模型相关接口")
public class ModelController {
    @Autowired
    private ModelService modelService;
    
    @PostMapping("/initialize")
    @Operation(summary = "初始化默认模型", description = "将预设的可用模型保存到数据库")
    public ResponseEntity<ApiResponse<String>> initializeDefaultModels() {

        try {
            modelService.initializeDefaultModels();
            return ResponseEntity.ok(ApiResponse.success("默认模型初始化成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("初始化模型失败: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "获取所有激活的模型", description = "获取系统中所有可用的AI模型列表")
    public ResponseEntity<ApiResponse<List<Model>>> getActiveModels() {
        try {
            List<Model> models = modelService.getActiveModels();
            return ResponseEntity.ok(ApiResponse.success(models));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取模型列表失败: " + e.getMessage()));
        }
    }
}

