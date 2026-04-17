package com.fededev.cloudstorage.folder.repository;

import com.fededev.cloudstorage.folder.model.Folder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    @Query("SELECT f FROM Folder f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<Folder> findActiveById(UUID id);

    @Query("SELECT f FROM Folder f WHERE f.id = :id AND f.deletedAt IS NOT NULL")
    Optional<Folder> findDeletedById(UUID id);

    @Query("""
        SELECT COUNT(*) > 0 
        FROM Folder f
        WHERE f.id = :id
        AND f.workspace.id = :workspaceId
        AND f.deletedAt IS NULL
    """)
    boolean existsByIdAndWorkspace(@Param("id") UUID id,  @Param("workspaceId") UUID workspaceId);

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

    @Lock(LockModeType.OPTIMISTIC)
    @Query("""
        SELECT f FROM Folder f
        WHERE f.id = :targetFolderId
            AND f.workspace.id = :workspaceId
            AND f.deletedAt IS NULL
    """)
    Optional<Folder> findActiveForUpdate(
            @Param("targetFolderId") UUID targetFolderId,
            @Param("workspaceId") UUID workspaceId
    );

    @Query("""
        SELECT f FROM Folder f
        WHERE f.workspace.id = :workspaceId 
        AND f.parent IS NULL 
        AND f.deletedAt IS NULL
    """)
    Page<Folder> findRootFolders(@Param("workspaceId") UUID workspaceId, Pageable pageable);

    @Query("""
        SELECT f FROM Folder f
        JOIN FETCH f.workspace w
        WHERE f.id = :folderId
            AND f.deletedAt IS NULL
    """)
    Optional<Folder> findActiveByIdWithWorkspace(UUID folderId);

    @Query("""
        SELECT f FROM Folder f
        WHERE f.id = :folderId
            AND f.deletedAt IS NULL
            AND EXISTS (
                SELECT 1 FROM WorkspaceMember wm 
                WHERE wm.workspace.id = f.workspace.id 
                AND wm.user.id = :userId
            )
    """)
    Optional<Folder> findByIdAndUserAccess(@Param("folderId") UUID folderId, @Param("userId") UUID userId);

    Page<Folder> findByParentIdAndDeletedAtIsNull(UUID parentId, Pageable pageable);

    @Query(value = """
        WITH RECURSIVE parent_hierarchy AS (
            SELECT id, parent_id
            FROM folders
            WHERE id = :destinationId
              AND deleted_at IS NULL
            UNION ALL

            SELECT f.id, f.parent_id
            FROM folders f
            INNER JOIN parent_hierarchy ph ON ph.parent_id = f.id
            WHERE f.deleted_at IS NULL
        )
        SELECT COUNT(1)
        FROM parent_hierarchy
        WHERE id = :folderToMoveId
    """, nativeQuery = true)
    long isDescendant(
            @Param("destinationId") UUID destinationId,
            @Param("folderToMoveId") UUID folderToMoveId
    );

    @Modifying(clearAutomatically = true)
    @Query(value = """
        WITH RECURSIVE target_folders AS (
            SELECT id
            FROM folders
            WHERE id = :folderId
              AND deleted_at IS NULL
            
            UNION ALL
            
            SELECT f.id
            FROM folders f
            INNER JOIN target_folders tf ON f.parent_id = tf.id
            WHERE f.deleted_at IS NULL
        )
        UPDATE folders
        SET deleted_at = NOW()
        WHERE id IN (SELECT id FROM target_folders)
        """, nativeQuery = true)
    void softDeleteFolderAndSubfolders(@Param("folderId") UUID folderId);

    @Query("""
        SELECT f FROM Folder f 
        LEFT JOIN Folder p ON f.parent.id = p.id
        WHERE f.workspace.id = :workspaceId
            AND f.deletedAt IS NOT NULL 
            AND (f.parent IS NULL OR p.deletedAt IS NULL)
        ORDER BY f.deletedAt DESC
    """)
    Page<Folder> findTrashRootFolders(@Param("workspaceId") UUID workspaceId, Pageable pageable);

    @Query("SELECT f FROM Folder f WHERE f.parent.id = :parentId AND f.deletedAt IS NOT NULL ORDER BY f.deletedAt DESC")
    Page<Folder> findTrashedSubfolders(@Param("parentId") UUID parentId, Pageable pageable);

    @Query("""
        SELECT f FROM Folder f
        WHERE f.workspace.id = :workspaceId
        AND f.deletedAt IS NOT NULL
    """)
    List<Folder> findTrashedByWorkspace(@Param("workspaceId") UUID workspaceId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Folder f WHERE f.id IN :ids")
    int deleteByIds(@Param("ids") List<UUID> ids);

}
