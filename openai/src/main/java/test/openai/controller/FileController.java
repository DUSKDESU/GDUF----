package test.openai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import test.openai.dto.response.ApiResponse;
import test.openai.dto.response.FileResponse;
import test.openai.service.FileService;

@RestController
@RequestMapping("/v1/files")
@Tag(name = "文件管理", description = "OpenAI Files API 兼容接口")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "上传文件到服务器")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestPart("file") @Parameter(description = "要上传的文件") MultipartFile file,
            @RequestPart("purpose") @Parameter(description = "文件用途，如：fine-tune") String purpose) {

        try {
            Long userId = getCurrentUserId();
            FileResponse response = fileService.uploadFile(file, purpose, userId);
            return ResponseEntity.ok(ApiResponse.success("文件上传成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件上传失败: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "获取文件列表", description = "获取当前用户的所有文件列表")
    public ResponseEntity<ApiResponse<FileResponse>> listFiles() {
        try {
            Long userId = getCurrentUserId();
            FileResponse response = fileService.listFiles(userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取文件列表失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{file_id}")
    @Operation(summary = "获取文件元信息", description = "获取指定文件的详细信息")
    public ResponseEntity<ApiResponse<FileResponse>> getFileInfo(
            @PathVariable("file_id") @Parameter(description = "文件ID") String fileId) {

        try {
            Long userId = getCurrentUserId();
            FileResponse response = fileService.getFileInfo(fileId, userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取文件信息失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{file_id}")
    @Operation(summary = "删除文件", description = "删除指定的文件")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @PathVariable("file_id") @Parameter(description = "文件ID") String fileId) {

        try {
            Long userId = getCurrentUserId();
            fileService.deleteFile(fileId, userId);
            return ResponseEntity.ok(ApiResponse.success("文件删除成功", fileId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件删除失败: " + e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        return 1L;
    }
}
