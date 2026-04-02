package com.fededev.cloudstorage.workspace.model;

import com.fededev.cloudstorage.common.AuditableEntity;
import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workspaces")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Workspace extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    private String name;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkspaceMember> members = new ArrayList<>();

    @NotNull
    private Long totalQuota;

    @NotNull
    @PositiveOrZero
    private Long usedStorage;

    public void addMember(AppUser user, WorkspaceRole role) {

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(this)
                .user(user)
                .role(role)
                .build();

        this.members.add(member);
    }

    public void removeMember(WorkspaceMember member) {
        this.members.remove(member);
        member.setWorkspace(null);
    }

    public boolean hasAvailableStorage(long fileSize) {
        return this.usedStorage + fileSize <= this.totalQuota;
    }

    public void consumeStorage(long size) {
        if (!hasAvailableStorage(size)) {
            throw new AppException(ErrorCode.WORKSPACE_QUOTA_EXCEEDED);
        }
        this.usedStorage += size;
    }

    public void releaseStorage(long size) {
            this.usedStorage = Math.max(0, this.usedStorage - size);
    }

}
