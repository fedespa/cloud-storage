package com.fededev.cloudstorage.workspace.invitations.request;

import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InvitationRequest(

        @Email @NotBlank
        String email,

        @NotNull
        WorkspaceRole role

) {
}
