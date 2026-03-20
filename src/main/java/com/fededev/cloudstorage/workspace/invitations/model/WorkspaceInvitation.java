package com.fededev.cloudstorage.workspace.invitations.model;

import com.fededev.cloudstorage.common.AuditableEntity;
import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.model.Workspace;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workspace_invitations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WorkspaceInvitation extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Email
    private String email;

    @JoinColumn(name = "invited_user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser invitedUser;

    @JoinColumn(name = "workspace_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Workspace workspace;

    @JoinColumn(name = "invited_by")
    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser invitedBy;

    @Enumerated(EnumType.STRING)
    @NotNull
    private WorkspaceRole role;

    @NotBlank
    private String token;

    @NotNull
    private Instant expiresAt;

    @Builder.Default
    @NotNull
    private boolean accepted = false;

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public void markAsAccepted() {
        this.accepted = true;
    }

    public void validateCanBeAccepted() {
        if (this.isExpired()) {
            throw new AppException(ErrorCode.INVITATION_EXPIRED);
        }

        if (this.isAccepted()) {
            throw new AppException(ErrorCode.INVITATION_ALREADY_ACCEPTED);
        }
    }

}
