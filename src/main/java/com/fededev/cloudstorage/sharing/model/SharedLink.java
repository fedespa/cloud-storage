package com.fededev.cloudstorage.sharing.model;

import com.fededev.cloudstorage.common.AuditableEntity;
import com.fededev.cloudstorage.file.model.File;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shared_links")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SharedLink extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @NotNull
    private Instant expiresAt;

    @Builder.Default
    private boolean revoked = false;

    public boolean isExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }

}
