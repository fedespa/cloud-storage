package com.fededev.cloudstorage.workspace.invitations.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(

        @NotBlank
        String token

) {
}
