package com.fededev.cloudstorage.auth;

import com.fededev.cloudstorage.auth.request.RegisterRequest;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.service.UserService;
import com.fededev.cloudstorage.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final WorkspaceService workspaceService;

    @Transactional
    public void handleRegistration(RegisterRequest request) {

        AppUser user = this.userService.register(request);
        // TODO: send email confirmation

        this.workspaceService.createDefaultWorkspace(user);
    }

}
