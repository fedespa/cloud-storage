package com.fededev.cloudstorage.folder.model;

import com.fededev.cloudstorage.common.AuditableEntity;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.model.Workspace;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "folders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Folder extends AuditableEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    private Instant deletedAt;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Folder> children;

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY)
    private List<File> files;

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isOwnerOfFolder(UUID userId) {
        return this.owner.getId().equals(userId);
    }

    public void moveTo(Folder folder){
        this.parent = folder;
    }

    public void markAsDeleted(){
        this.deletedAt = Instant.now();
    }
}
