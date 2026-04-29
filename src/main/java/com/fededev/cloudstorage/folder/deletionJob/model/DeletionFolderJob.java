package com.fededev.cloudstorage.folder.deletionJob.model;

import com.fededev.cloudstorage.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deletion_folder_jobs")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletionFolderJob extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @NotNull
    private UUID folderId;

    @NotNull
    private UUID requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeletionFolderJobStatus status;

    private Instant startedAt;

    @NotNull
    @Builder.Default
    private int attempts = 0;

    private Instant finishedAt;

    private String errorMessage;

    public void markInProgress() {
        this.status = DeletionFolderJobStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }

    public void markPending() {
        this.status = DeletionFolderJobStatus.PENDING;
    }

    public void increaseAttempts() {
        this.attempts += 1;
    }

    public void markCompleted() {
        this.status = DeletionFolderJobStatus.COMPLETED;
        this.finishedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = DeletionFolderJobStatus.FAILED;
        this.finishedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

}
