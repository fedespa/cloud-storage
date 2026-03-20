package com.fededev.cloudstorage.workspace.invitations.response;

import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceMembership(
        UUID workspaceId,
        String workspaceName,
        WorkspaceRole role,
        Instant joinedAt
) {
}
