package com.fededev.cloudstorage.file.model;

import com.fededev.cloudstorage.common.AuditableEntity;
import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.model.Workspace;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class File extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    private String extension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false)
    @NotNull
    private Long size;

    @Column(name = "s3_key", unique = true)
    private String s3Key;

    @NotBlank
    private String mimeType;

    private Instant deletedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileStatus status = FileStatus.PENDING;

    private Instant confirmedAt;

    @Version
    private Long version;

    public boolean isOwnerOfFile(UUID userId) {
        return this.owner.getId().equals(userId);
    }

    public void markAsDeleted(){
        this.deletedAt = Instant.now();
    }

    public void markAsFailed(){
        this.status = FileStatus.FAILED;
    }

    public void markAsUploaded(){
        this.status = FileStatus.UPLOADED;
        this.confirmedAt = Instant.now();
    }

    public void changeFolder(Folder newFolder){
        if ((this.folder != null && this.folder.equals(newFolder)) || (this.folder == null && newFolder == null)) {
            throw new AppException(ErrorCode.FILE_ALREADY_IN_FOLDER);
        }
        this.folder = newFolder;
    }

}
