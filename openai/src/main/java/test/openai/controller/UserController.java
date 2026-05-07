package test.openai.controller;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import test.openai.dto.response.ApiResponse;
import test.openai.dto.response.UserResponse;
import test.openai.entity.User;
import test.openai.repository.UserRepository;
import test.openai.service.UserService;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "处理更新查询修改")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未认证用户");
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        return user.getId();
    }


    @GetMapping("/profile")
    @Operation(summary = "获取用户资料", description = "获取用户的信息")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            UserResponse response = userService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户资料失败: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户资料", description = "更新当前用户信息")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication,
            @RequestBody UserResponse request) {
        try {
            Long userId = getCurrentUserId(authentication);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            
            userRepository.save(user);
            
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setIsActive(user.getIsActive());
            response.setCreatedAt(user.getCreatedAt());
            response.setUpdatedAt(user.getUpdatedAt());
            
            return ResponseEntity.ok(ApiResponse.success("更新成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新用户资料失败: " + e.getMessage()));
        }
    }

    @PutMapping("/changepassword")
    @Operation(summary = "修改密码", description = "修改当前密码")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            Long userId = getCurrentUserId(authentication);
            
            userService.updatePassword(userId, oldPassword, newPassword);

            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("密码修改失败: " + e.getMessage()));
        }
    }
}
