package com.a0000.digicompass.modules.preference.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import com.a0000.digicompass.modules.auth.service.AuthService;
import com.a0000.digicompass.modules.preference.dto.UserPreferenceItem;
import com.a0000.digicompass.modules.preference.dto.UserPreferenceSaveRequest;
import com.a0000.digicompass.modules.preference.service.PreferenceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;
    private final AuthService authService;

    public PreferenceController(PreferenceService preferenceService, AuthService authService) {
        this.preferenceService = preferenceService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ApiResponse<UserPreferenceItem> get() {
        LoginUser user = authService.currentUser();
        return ApiResponse.success(preferenceService.getPreference(user.id()));
    }

    @PutMapping("/me")
    public ApiResponse<Void> save(@RequestBody UserPreferenceSaveRequest request) {
        LoginUser user = authService.currentUser();
        preferenceService.savePreference(user.id(), request);
        return ApiResponse.success(null);
    }
}
