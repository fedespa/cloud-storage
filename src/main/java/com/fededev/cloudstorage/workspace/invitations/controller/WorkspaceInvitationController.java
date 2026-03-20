package com.fededev.cloudstorage.workspace.invitations.controller;

import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.workspace.invitations.request.AcceptInvitationRequest;
import com.fededev.cloudstorage.workspace.invitations.request.InvitationRequest;
import com.fededev.cloudstorage.workspace.invitations.response.WorkspaceInvitationDto;
import com.fededev.cloudstorage.workspace.invitations.response.WorkspaceMembership;
import com.fededev.cloudstorage.workspace.invitations.service.WorkspaceInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceInvitationController {

    private final WorkspaceInvitationService workspaceInvitationService;

    @PostMapping("/{id}/invite")
    public ResponseEntity<?> invite(
            @PathVariable String id,
            @Valid @RequestBody InvitationRequest invitationRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        WorkspaceInvitationDto invitation = this.workspaceInvitationService.invite(
                UUID.fromString(id),
                invitationRequest,
                userDetails
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }

    @PostMapping("/accept")
    public ResponseEntity<WorkspaceMembership> acceptInvitation(
            @RequestParam(name = "token") String token,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){

        WorkspaceMembership membership = this.workspaceInvitationService.acceptInvitation(
                new AcceptInvitationRequest(token),
                userDetails.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

}
