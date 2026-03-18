package com.fededev.cloudstorage.workspace.model;

import com.fededev.cloudstorage.user.model.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workspace_members")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WorkspaceMember {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @JoinColumn(name = "workspace_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Workspace workspace;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @NotNull
    private WorkspaceRole role;

    @NotNull
    @Builder.Default
    private Instant joinedAt = Instant.now();

}
