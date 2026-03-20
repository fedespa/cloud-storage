package com.fededev.cloudstorage.workspace.invitations.repository;

import com.fededev.cloudstorage.workspace.invitations.model.WorkspaceInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID> {

    @Query("""
        SELECT COUNT(i) > 0
        FROM WorkspaceInvitation i
        WHERE i.workspace.id = :workspaceId
        AND i.email = :email
        AND i.expiresAt > :now
    """)
    boolean existsActiveInvitation(
            @Param("workspaceId") UUID workspaceId,
            @Param("email") String email,
            @Param("now") Instant now
    );

    Optional<WorkspaceInvitation> findByToken(String token);

}