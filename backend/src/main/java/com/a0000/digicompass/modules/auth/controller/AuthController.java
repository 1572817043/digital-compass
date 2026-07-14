package com.a0000.digicompass.modules.auth.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.auth.dto.LoginRequest;
import com.a0000.digicompass.modules.auth.dto.LoginResponse;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import com.a0000.digicompass.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<LoginUser> me() {
        return ApiResponse.success(authService.currentUser());
    }
}
