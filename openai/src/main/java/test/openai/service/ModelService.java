package test.openai.service;

import test.openai.dto.request.ModelRequest;
import test.openai.entity.Model;

import java.util.List;

public interface ModelService {


     //初始化默认模型列表到数据库

    void initializeDefaultModels();


    // 获取所有激活的模型

    List<Model> getActiveModels();



     // 添加新模型

    Model addModel(ModelRequest modelRequest, Long ownerId);

}
