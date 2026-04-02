package com.fededev.cloudstorage.trash.model;

import com.fededev.cloudstorage.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trash_jobs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TrashJob extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    private UUID workspaceId;

    @NotNull
    private UUID requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrashJobStatus status;

    private Instant startedAt;

    private Instant finishedAt;

    private String errorMessage;

    public void markInProgress() {
        this.status = TrashJobStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }

    public void markCompleted() {
        this.status = TrashJobStatus.COMPLETED;
        this.finishedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = TrashJobStatus.FAILED;
        this.finishedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

}
