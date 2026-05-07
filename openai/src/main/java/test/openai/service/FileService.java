package test.openai.service;

import org.springframework.web.multipart.MultipartFile;

import test.openai.dto.response.FileResponse;

public interface FileService {


      //上传文件

    FileResponse uploadFile(MultipartFile file, String purpose, Long userId);


     // 删除文件

    void deleteFile(String fileId, Long userId);


     // 获取文件列表

    FileResponse listFiles(Long userId);


    // 获取文件元信息

    FileResponse getFileInfo(String fileId, Long userId);
}
