package com.a0000.digicompass.modules.auth.entity;

public record UserAccount(
        Long id,
        String username,
        String passwordHash,
        String nickname,
        String role,
        int status
) {
}
