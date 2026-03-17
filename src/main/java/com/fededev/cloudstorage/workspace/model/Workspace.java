package com.fededev.cloudstorage.workspace.model;

import com.fededev.cloudstorage.common.AuditableEntity;
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

    public void addMember(WorkspaceMember member) {
        this.members.add(member);
        member.setWorkspace(this);
    }

    public void removeMember(WorkspaceMember member) {
        this.members.remove(member);
        member.setWorkspace(null);
    }

}
