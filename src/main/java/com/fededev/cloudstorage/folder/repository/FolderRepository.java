package com.fededev.cloudstorage.folder.repository;

import com.fededev.cloudstorage.folder.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    @Query("""
        SELECT COUNT(*) > 0 
        FROM Folder f
        WHERE f.id = :id
        AND f.workspace.id = :workspaceId
        AND f.deletedAt IS NULL
    """)
    boolean existsByIdAndWorkspace(
            @Param("id") UUID id,
            @Param("workspaceId") UUID workspaceId
    );

    @Query("""
        SELECT COUNT(*) > 0
        FROM Folder f
        WHERE f.workspace.id = :workspaceId
        AND f.name = :name
        AND (:parentId IS NULL AND f.parent.id IS NULL OR f.parent.id = :parentId)
        AND f.deletedAt IS NULL
    """)
    boolean existsByNameInLevel(
            @Param("name") String name,
            @Param("workspaceId") UUID workspaceId,
            @Param("parentId") UUID parentId
    );

    Optional<Folder> findByIdAndWorkspaceId(UUID id, UUID workspaceId);

}
