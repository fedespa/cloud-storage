package com.fededev.cloudstorage.file.repository;

import com.fededev.cloudstorage.file.model.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {

    Page<File> findByFolderIdAndDeletedAtIsNull(UUID folderId, Pageable pageable);

    @Query("""
        SELECT f FROM File f
        WHERE f.workspace.id = :workspaceId
        AND f.folder.id IS NULL
        AND f.deletedAt IS NULL
    """)
    Page<File> findRootFiles(@Param("workspaceId") UUID workspaceId, Pageable pageable);

}
