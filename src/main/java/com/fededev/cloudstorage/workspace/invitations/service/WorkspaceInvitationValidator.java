package com.fededev.cloudstorage.workspace.invitations.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.repository.UserRepository;
import com.fededev.cloudstorage.workspace.invitations.repository.WorkspaceInvitationRepository;
import com.fededev.cloudstorage.workspace.invitations.request.InvitationRequest;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import com.fededev.cloudstorage.workspace.model.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkspaceInvitationValidator {

    private final WorkspaceMemberService workspaceMemberService;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;
    private final UserRepository userRepository;

    public Optional<AppUser> validateInvitation(Workspace workspace, InvitationRequest request, CustomUserDetails userDetails){
        boolean isAdminOrOwner = this.workspaceMemberService.isAdminOrOwner(workspace.getId(), userDetails.getId());

        if (!isAdminOrOwner) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        if (this.workspaceInvitationRepository.existsActiveInvitation(workspace.getId(), request.email(), Instant.now())) {
            throw new AppException(ErrorCode.WORKSPACE_INVITATION_ALREADY_SENT);
        }

        Optional<AppUser> user = this.userRepository.findByEmail(request.email());

        if (user.isPresent()) {
            boolean invitedUserIsMember = this.workspaceMemberService.isMember(workspace.getId(), user.get().getId());

            if (invitedUserIsMember) {
                throw new AppException(ErrorCode.WORKSPACE_MEMBER_ALREADY_EXISTS);
            }
        }

        return user;
    }


}
