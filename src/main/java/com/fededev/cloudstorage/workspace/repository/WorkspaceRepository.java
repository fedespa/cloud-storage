package com.fededev.cloudstorage.workspace.repository;

import com.fededev.cloudstorage.workspace.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    @Modifying
    @Query("""
    UPDATE Workspace w 
    SET w.usedStorage = w.usedStorage + :size 
    WHERE w.id = :id 
    AND (w.usedStorage + :size) <= w.totalQuota
""")
    int increaseUsedStorageIfPossible(@Param("id") UUID id, @Param("size") Long size);

    @Modifying
    @Query("UPDATE Workspace w SET w.usedStorage = w.usedStorage - :size WHERE w.id = :id")
    void decreaseUsedStorage(@Param("id") UUID id, @Param("size") Long size);

}
