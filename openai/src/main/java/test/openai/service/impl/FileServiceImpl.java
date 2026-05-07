package test.openai.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import test.openai.dto.response.FileResponse;
import test.openai.entity.File;
import test.openai.repository.FileRepository;
import test.openai.service.FileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public FileResponse uploadFile(MultipartFile file, String purpose, Long userId) {
        log.info("开始上传文件: {}, 用户ID: {}", file.getOriginalFilename(), userId);
        
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileId = "file-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String newFileName = fileId + extension;
        Path uploadPath = Paths.get(uploadDir);
        
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path targetPath = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            File fileEntity = new File();
            fileEntity.setFileId(fileId);
            fileEntity.setUserId(userId);
            fileEntity.setFilename(originalFileName);
            fileEntity.setBytes(file.getSize());
            fileEntity.setPurpose(purpose != null ? purpose : "fine-tune");
            fileEntity.setStatus("uploaded");
            fileEntity.setStatusDetails("文件上传成功");
            
            File savedFile = fileRepository.save(fileEntity);
            
            log.info("文件上传成功: {}, fileId: {}", savedFile.getFilename(), savedFile.getFileId());
            
            return FileResponse.builder()
                    .id(savedFile.getFileId())
                    .object("file")
                    .bytes(savedFile.getBytes())
                    .createdAt(savedFile.getCreatedAt())
                    .filename(savedFile.getFilename())
                    .purpose(savedFile.getPurpose())
                    .status(savedFile.getStatus())
                    .build();
                    
        } catch (IOException e) {
            log.error("文件上传失败: {}", originalFileName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileId, Long userId) {
        log.info("开始删除文件: {}, 用户ID: {}", fileId, userId);
        
        File file = fileRepository.findByFileId(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在: " + fileId));
        
        if (!file.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此文件");
        }
        
        try {
            Path uploadPath = Paths.get(uploadDir);
            
            String extension = "";
            if (file.getFilename() != null && file.getFilename().contains(".")) {
                extension = file.getFilename().substring(file.getFilename().lastIndexOf("."));
            }
            Path filePath = uploadPath.resolve(file.getFileId() + extension);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            fileRepository.deleteByFileId(fileId);
            log.info("文件删除成功: {}", fileId);
            
        } catch (IOException e) {
            log.error("删除物理文件失败: {}", fileId, e);
            throw new RuntimeException("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public FileResponse listFiles(Long userId) {
        log.info("获取文件列表, 用户ID: {}", userId);
        
        List<File> files = fileRepository.findByUserId(userId);
        
        List<FileResponse> fileResponses = files.stream()
                .map(file -> FileResponse.builder()
                        .id(file.getFileId())
                        .object("file")
                        .bytes(file.getBytes())
                        .createdAt(file.getCreatedAt())
                        .filename(file.getFilename())
                        .purpose(file.getPurpose())
                        .status(file.getStatus())
                        .build())
                .toList();
        
        return FileResponse.builder()
                .object("list")
                .data(fileResponses)
                .build();
    }

    @Override
    public FileResponse getFileInfo(String fileId, Long userId) {
        log.info("获取文件元信息: {}, 用户ID: {}", fileId, userId);
        
        File file = fileRepository.findByFileId(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在: " + fileId));
        
        if (!file.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此文件");
        }
        
        return FileResponse.builder()
                .id(file.getFileId())
                .object("file")
                .bytes(file.getBytes())
                .createdAt(file.getCreatedAt())
                .filename(file.getFilename())
                .purpose(file.getPurpose())
                .status(file.getStatus())
                .build();
    }
}
