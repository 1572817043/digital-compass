package com.a0000.digicompass.modules.user.dto;

public record UserListItem(
        Long id,
        String username,
        String nickname,
        String role,
        int status,
        String createdAt,
        String updatedAt
) {
}
