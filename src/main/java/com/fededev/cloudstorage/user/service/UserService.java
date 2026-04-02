package com.fededev.cloudstorage.user.service;

import com.fededev.cloudstorage.auth.request.RegisterRequest;
import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUser register(RegisterRequest request) {

        boolean exists = this.userRepository.existsByEmail(request.email().toLowerCase().trim());

        if (exists) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String hashPassword = this.passwordEncoder.encode(request.password());

        AppUser user = AppUser.builder()
                .email(request.email().toLowerCase().trim())
                .password(hashPassword)
                .build();

        AppUser savedUser = this.userRepository.save(user);

        return savedUser;
    }



}
