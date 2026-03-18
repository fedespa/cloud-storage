package com.fededev.cloudstorage.auth;

import com.fededev.cloudstorage.auth.request.LoginRequest;
import com.fededev.cloudstorage.auth.request.RegisterRequest;
import com.fededev.cloudstorage.auth.response.AccessTokenResponse;
import com.fededev.cloudstorage.auth.response.TokensResponse;
import com.fededev.cloudstorage.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${refresh.token.expiration}")
    private long cookieMaxAge;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ){

        this.authService.handleRegistration(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ){

        TokensResponse tokens = this.authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
                .httpOnly(true)
                .secure(false) // TRUE EN PROD
                .path("/")
                .maxAge(this.cookieMaxAge)
                .sameSite("Strict")
                // .domain()
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).body(
                new AccessTokenResponse(tokens.accessToken())
        );

    }

}
