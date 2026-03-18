package com.fededev.cloudstorage.auth.model;

import com.fededev.cloudstorage.common.AuditableEntity;
import com.fededev.cloudstorage.user.model.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@AllArgsConstructor @NoArgsConstructor
@Getter
@Setter
@Builder
public class RefreshToken extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String token;

    @NotNull
    private Instant expiresAt;

    @NotNull
    @Builder.Default
    private boolean revoked = false;

    public boolean isExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }

}
