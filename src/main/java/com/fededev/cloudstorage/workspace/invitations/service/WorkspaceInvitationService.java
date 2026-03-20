package com.fededev.cloudstorage.workspace.invitations.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.infraestructure.security.utils.HashUtils;
import com.fededev.cloudstorage.infraestructure.security.utils.TokenWithHash;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.repository.UserRepository;
import com.fededev.cloudstorage.workspace.invitations.event.InvitationCreatedEvent;
import com.fededev.cloudstorage.workspace.invitations.request.AcceptInvitationRequest;
import com.fededev.cloudstorage.workspace.invitations.response.WorkspaceInvitationDto;
import com.fededev.cloudstorage.workspace.invitations.response.WorkspaceMembership;
import com.fededev.cloudstorage.workspace.model.Workspace;
import com.fededev.cloudstorage.workspace.invitations.model.WorkspaceInvitation;
import com.fededev.cloudstorage.workspace.invitations.repository.WorkspaceInvitationRepository;
import com.fededev.cloudstorage.workspace.repository.WorkspaceRepository;
import com.fededev.cloudstorage.workspace.invitations.request.InvitationRequest;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceInvitationService {

    private final WorkspaceMemberService workspaceMemberService;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final HashUtils hashUtils;
    private final WorkspaceInvitationValidator validator;

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public WorkspaceInvitationDto invite(UUID workspaceId, InvitationRequest request, CustomUserDetails userDetails) {

        Workspace workspace = this.workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_NOT_FOUND));

        Optional<AppUser> invitedUser = this.validator.validateInvitation(workspace, request, userDetails);

        AppUser invitedBy = this.userRepository.getReferenceById(userDetails.getId());

        TokenWithHash tokenPair = this.hashUtils.generateHashToken();

        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email(request.email())
                .invitedUser(invitedUser.orElse(null))
                .workspace(workspace)
                .invitedBy(invitedBy)
                .role(request.role())
                .token(tokenPair.hashedToken())
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        WorkspaceInvitation savedInvitation = this.workspaceInvitationRepository.save(invitation);

        this.eventPublisher.publishEvent(new InvitationCreatedEvent(
                savedInvitation.getId(),
                savedInvitation.getEmail(),
                workspace.getName(),
                tokenPair.rawToken()
        ));

        return new WorkspaceInvitationDto(
                savedInvitation.getId(),
                savedInvitation.getEmail(),
                savedInvitation.getExpiresAt(),
                savedInvitation.getRole()
        );
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public WorkspaceMembership acceptInvitation(
            AcceptInvitationRequest request,
            UUID userId
    ) {
        String hashedToken = this.hashUtils.sha256(request.token());

        WorkspaceInvitation invitation = this.workspaceInvitationRepository.findByToken(hashedToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

        invitation.validateCanBeAccepted();

        Workspace workspace = invitation.getWorkspace();

        AppUser targetUser = resolveTargetUser(invitation, userId);

        this.workspaceMemberService.validateWorkspaceLimit(targetUser.getId());

        if (this.workspaceMemberService.isMember(workspace.getId(), targetUser.getId())) {
            throw new AppException(ErrorCode.WORKSPACE_MEMBER_ALREADY_EXISTS);
        }

        workspace.addMember(targetUser, invitation.getRole());

        invitation.markAsAccepted();

        return new WorkspaceMembership(
                workspace.getId(),
                workspace.getName(),
                invitation.getRole(),
                Instant.now()
        );
    }

    private AppUser resolveTargetUser(
            WorkspaceInvitation invitation,
            UUID userId
    ) {
        AppUser invitedUser = invitation.getInvitedUser();

        if (invitedUser != null) {
            if (!invitedUser.getId().equals(userId)) {
                throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
            }

            return invitedUser;
        } else {
            AppUser user = this.userRepository.findByEmail(invitation.getEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED));

            if (!user.getId().equals(userId)) {
                throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
            }

            return user;
        }
    }


}
