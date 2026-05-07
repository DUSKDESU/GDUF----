package test.openai.service;

import test.openai.dto.request.UserLoginRequest;
import test.openai.dto.request.UserRegisterRequest;
import test.openai.dto.response.JwtResponse;
import test.openai.dto.response.UserResponse;
import test.openai.entity.User;

import java.util.List;

public interface UserService {
// 用户注册
    User register(UserRegisterRequest request);
// 用户登录
    JwtResponse login(UserLoginRequest request);
// 获取用户信息
    UserResponse getUserById(Long userId);
// 获取所有用户信息
    List<UserResponse> getAllUsers();
// 创建用户
    UserResponse createUser(UserResponse request);
// 删除用户
    void deleteUser(Long userId);
// 更新用户状态
    User updateUserStatus(Long userId, Boolean active);
// 获取用户用量
    Object getUserUsageStats(Long userId);

// 更新用户密码
    User updatePassword(Long userId, String oldPassword, String newPassword);
}
