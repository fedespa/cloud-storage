package com.fededev.cloudstorage.file.repository;

import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.FileStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("""
        SELECT f FROM File f
        JOIN FETCH f.workspace w
        WHERE f.id = :fileId
            AND f.deletedAt IS NULL
    """)
    Optional<File> findActiveTargetForUpdate(@Param("fileId") UUID fileId);

    @Query("""
        SELECT f FROM File f
        WHERE f.status = 'PENDING'
        AND f.createdAt < :cutoff
    """)
    List<File> findStaleUploads(@Param("cutoff") Instant cutoff, Limit limit);

    @Query("""
        SELECT f FROM File f
        WHERE f.id = :fileId
        AND f.owner.id = :ownerId
    """)
    Optional<File> findByIdAndOwnerId(
            UUID fileId,
            UUID ownerId
    );

    @Query("""
        SELECT f FROM File f
        WHERE f.id = :fileId
        AND f.owner.id = :ownerId
        AND f.status = :status
    """)
    Optional<File> findByIdAndOwnerIdAndStatus(
            UUID fileId,
            UUID ownerId,
            FileStatus status
    );

    @Query("""
        SELECT COUNT(f) > 0 FROM File f 
        WHERE f.name = :name 
          AND f.extension = :extension 
          AND f.workspace.id = :workspaceId 
          AND (:folderId IS NULL AND f.folder IS NULL OR f.folder.id = :folderId)
          AND f.deletedAt IS NULL
    """)
    boolean existsByNameAndContext(
            String name,
            String extension,
            UUID workspaceId,
            UUID folderId
    );

    Page<File> findByFolderIdAndDeletedAtIsNull(UUID folderId, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(value = """
        WITH RECURSIVE target_folders AS (
            SELECT id FROM folders WHERE id = :folderId AND deleted_at IS NULL
            UNION ALL
            SELECT f.id FROM folders f INNER JOIN target_folders tf ON f.parent_id = tf.id
            WHERE f.deleted_at IS NULL
        )
        UPDATE files
        SET deleted_at = NOW()
        WHERE folder_id IN (SELECT id FROM target_folders)
          AND deleted_at IS NULL
        """, nativeQuery = true)
    void softDeleteFilesInFolders(@Param("folderId") UUID folderId);

    @Query("""
        SELECT f FROM File f
        JOIN FETCH f.workspace w
        WHERE f.id = :fileId
            AND f.deletedAt IS NULL
    """)
    Optional<File> findActiveByIdWithWorkspace(@Param("fileId") UUID fileId);

    @Query("""
        SELECT f FROM File f
        WHERE f.workspace.id = :workspaceId
        AND f.folder.id IS NULL
        AND f.deletedAt IS NULL
    """)
    Page<File> findRootFiles(@Param("workspaceId") UUID workspaceId, Pageable pageable);

    @Query("""
        SELECT fi FROM File fi
        LEFT JOIN Folder p ON fi.folder.id = p.id
        WHERE fi.workspace.id = :workspaceId
            AND fi.deletedAt IS NOT NULL
            AND (fi.folder.id IS NULL OR p.deletedAt IS NULL) 
        ORDER BY fi.deletedAt DESC
    """)
    Page<File> findTrashRootFiles(@Param("workspaceId") UUID workspaceId, Pageable pageable);

    @Modifying
    @Query("""
        DELETE FROM File fi
        WHERE fi.id IN :ids
    """)
    int deleteByIds(@Param("ids") List<UUID> ids);

    @Query("SELECT fi FROM File fi WHERE fi.folder.id = :folderId AND fi.deletedAt IS NOT NULL ORDER BY fi.deletedAt DESC")
    Page<File> findTrashedFilesInFolder(@Param("folderId") UUID folderId, Pageable pageable);

    @Query("""
        SELECT fi FROM File fi
        WHERE fi.workspace.id = :workspaceId 
        AND fi.deletedAt IS NOT NULL 
    """)
    List<File> findTrashedByWorkspace(@Param("workspaceId") UUID workspaceId, Pageable pageable);

}