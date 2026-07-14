package com.a0000.digicompass.modules.user.service;

import com.a0000.digicompass.modules.user.dto.UserListItem;
import java.util.List;

public interface AdminUserService {
    List<UserListItem> listUsers(String keyword, String role, Integer status);
    void updateRole(Long id, String role, Long currentUserId);
    void updateStatus(Long id, int status, Long currentUserId);
    void resetPassword(Long id, Long currentUserId);
}
