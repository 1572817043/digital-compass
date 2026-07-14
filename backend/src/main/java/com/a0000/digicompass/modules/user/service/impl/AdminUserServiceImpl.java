package com.a0000.digicompass.modules.user.service.impl;

import com.a0000.digicompass.modules.user.dto.UserListItem;
import com.a0000.digicompass.modules.user.mapper.AdminUserMapper;
import com.a0000.digicompass.modules.user.service.AdminUserService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final AdminUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(AdminUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserListItem> listUsers(String keyword, String role, Integer status) {
        return userMapper.findUsers(keyword, role, status);
    }

    @Override
    public void updateRole(Long id, String role, Long currentUserId) {
        if (id.equals(currentUserId)) throw new IllegalArgumentException("不能修改自己的角色");
        if (!"ADMIN".equals(role) && !"USER".equals(role)) throw new IllegalArgumentException("角色只能是 ADMIN 或 USER");
        userMapper.updateRole(id, role);
    }

    @Override
    public void updateStatus(Long id, int status, Long currentUserId) {
        if (id.equals(currentUserId) && status == 0) throw new IllegalArgumentException("不能禁用自己");
        if (status != 0 && status != 1) throw new IllegalArgumentException("状态只能是 0 或 1");
        userMapper.updateStatus(id, status);
    }

    @Override
    public void resetPassword(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) throw new IllegalArgumentException("不能重置自己的密码");
        String hash = passwordEncoder.encode(DEFAULT_PASSWORD);
        userMapper.updatePassword(id, hash);
    }
}
