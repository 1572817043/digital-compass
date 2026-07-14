package com.a0000.digicompass.modules.auth.service;

import com.a0000.digicompass.modules.auth.dto.LoginRequest;
import com.a0000.digicompass.modules.auth.dto.LoginResponse;
import com.a0000.digicompass.modules.auth.dto.LoginUser;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginUser currentUser();
}
