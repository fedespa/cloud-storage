package com.fededev.cloudstorage.user.model;

import com.fededev.cloudstorage.common.AuditableEntity;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor @NoArgsConstructor
@Getter
@Setter @Builder
public class AppUser extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private Instant deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<WorkspaceMember> memberships = new ArrayList<>();

}
