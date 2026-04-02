package com.fededev.cloudstorage.trash.repository;

import com.fededev.cloudstorage.trash.model.TrashJob;
import com.fededev.cloudstorage.trash.model.TrashJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrashJobRepository extends JpaRepository<TrashJob, UUID> {

    List<TrashJob> findTop10ByStatusInOrderByCreatedAtAsc(List<TrashJobStatus> statuses);

    boolean existsByWorkspaceIdAndStatusIn(
            @Param("workspaceId") UUID workspaceId,
            List<TrashJobStatus> status
    );

}
