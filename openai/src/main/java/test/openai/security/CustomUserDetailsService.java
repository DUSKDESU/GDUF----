package test.openai.security;


import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import test.openai.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        test.openai.entity.User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        return buildUserDetails(user);
    }

    /**
     * 根据用户 ID 加载用户详情
     */
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        test.openai.entity.User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在 ID: " + id));
        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(test.openai.entity.User user) {
        String role = Boolean.TRUE.equals(user.getIsAdmin()) ? "ROLE_ADMIN" : "ROLE_USER";
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .accountExpired(!user.getIsActive())
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
}
