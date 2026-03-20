package com.fededev.cloudstorage.auth.service;

import com.fededev.cloudstorage.auth.request.LoginRequest;
import com.fededev.cloudstorage.auth.request.RegisterRequest;
import com.fededev.cloudstorage.auth.response.TokensResponse;
import com.fededev.cloudstorage.infraestructure.security.CreateJWTTokenDto;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.infraestructure.security.utils.JwtUtils;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.service.UserService;
import com.fededev.cloudstorage.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final WorkspaceService workspaceService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AppUser handleRegistration(RegisterRequest request) {

        AppUser user = this.userService.register(request);
        // TODO: send email confirmation

        this.workspaceService.createDefaultWorkspace(user);

        return user;
    }

    public TokensResponse login(LoginRequest request){
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        if (!authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        CreateJWTTokenDto createJWTTokenDto = new CreateJWTTokenDto(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.isDeleted()
        );

        String accessToken = this.jwtUtils.generateToken(createJWTTokenDto);
        String refreshToken = this.refreshTokenService.create(userDetails.getId());


        return new TokensResponse(accessToken, refreshToken);
    }

}
