package com.a0000.digicompass.modules.auth.service.impl;

import com.a0000.digicompass.common.security.JwtTokenProvider;
import com.a0000.digicompass.modules.auth.dto.LoginRequest;
import com.a0000.digicompass.modules.auth.dto.LoginResponse;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import com.a0000.digicompass.modules.auth.entity.UserAccount;
import com.a0000.digicompass.modules.auth.service.AuthService;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserAccount userAccount = findByUsername(request.username());
        if (userAccount == null || userAccount.status() != 1) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.password(), userAccount.passwordHash())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        LoginUser loginUser = new LoginUser(
                userAccount.id(),
                userAccount.username(),
                userAccount.nickname(),
                userAccount.role()
        );
        String token = jwtTokenProvider.createToken(loginUser);
        return new LoginResponse(token, "Bearer", jwtTokenProvider.expirationSeconds(), loginUser);
    }

    @Override
    public LoginUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BadCredentialsException("未登录");
        }
        return loginUser;
    }

    private UserAccount findByUsername(String username) {
        List<UserAccount> users = jdbcTemplate.query(
                """
                SELECT id, username, password_hash, nickname, role, status
                FROM dc_user
                WHERE username = ?
                """,
                (rs, rowNum) -> new UserAccount(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("nickname"),
                        rs.getString("role"),
                        rs.getInt("status")
                ),
                username
        );
        return users.isEmpty() ? null : users.getFirst();
    }
}
