package com.fededev.cloudstorage.folder.deletionJob.repository;

import com.fededev.cloudstorage.folder.deletionJob.model.DeletionFolderJob;
import com.fededev.cloudstorage.folder.deletionJob.model.DeletionFolderJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeletionFolderJobRepository extends JpaRepository<DeletionFolderJob, UUID> {

    boolean existsByFolderId(UUID folderId);

    @Query("""
        SELECT j FROM DeletionFolderJob j
        WHERE j.status = 'PENDING' OR (j.status = 'IN_PROGRESS' AND j.updatedAt < :thresholdDate)
    """)
    List<DeletionFolderJob> findJobsToProcess(@Param("thresholdDate") Instant thresholdDate);

}
