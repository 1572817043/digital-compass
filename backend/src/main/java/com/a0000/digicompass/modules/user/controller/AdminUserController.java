package com.a0000.digicompass.modules.user.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import com.a0000.digicompass.modules.auth.service.AuthService;
import com.a0000.digicompass.modules.user.dto.UserListItem;
import com.a0000.digicompass.modules.user.service.AdminUserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService userService;
    private final AuthService authService;

    public AdminUserController(AdminUserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<UserListItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.success(userService.listUsers(keyword, role, status));
    }

    @PutMapping("/{id}/role")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @RequestParam String role) {
        LoginUser current = authService.currentUser();
        userService.updateRole(id, role, current.id());
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam int status) {
        LoginUser current = authService.currentUser();
        userService.updateStatus(id, status, current.id());
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/password/reset")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        LoginUser current = authService.currentUser();
        userService.resetPassword(id, current.id());
        return ApiResponse.success(null);
    }
}
