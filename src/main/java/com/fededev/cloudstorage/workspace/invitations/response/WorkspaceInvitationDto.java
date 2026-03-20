package com.fededev.cloudstorage.workspace.invitations.response;

import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceInvitationDto(

        UUID id,
        String invitedTo,
        Instant expiresAt,
        WorkspaceRole role

) {
}
