package com.fededev.cloudstorage.workspace.invitations.event;

import java.util.UUID;

public record InvitationCreatedEvent(
        UUID invitationId,
        String email,
        String workspaceName,
        String token
) {
}
