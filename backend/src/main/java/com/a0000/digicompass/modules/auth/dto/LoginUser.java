package com.a0000.digicompass.modules.auth.dto;

public record LoginUser(
        Long id,
        String username,
        String nickname,
        String role
) {
}
