package com.a0000.digicompass.modules.auth.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        LoginUser user
) {
}
