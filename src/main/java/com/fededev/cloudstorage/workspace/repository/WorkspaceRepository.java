package com.fededev.cloudstorage.workspace.repository;

import com.fededev.cloudstorage.workspace.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    @Query("""
        SELECT w FROM Workspace w
        WHERE w.id = :workspaceId
        AND EXISTS (
            SELECT 1 FROM WorkspaceMember wm
            WHERE wm.workspace.id = w.id
            AND wm.user.id = :userId
        )
    """)
    Optional<Workspace> findByIdAndUserAccess(UUID workspaceId, UUID userId);

}
