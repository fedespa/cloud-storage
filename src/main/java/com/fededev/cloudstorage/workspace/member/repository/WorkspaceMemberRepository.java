package com.fededev.cloudstorage.workspace.member.repository;

import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    boolean existsByWorkspaceIdAndUserIdAndRole(
            @Param("workspaceId") UUID workspaceId,
            @Param("userId") UUID userId,
            @Param("role") WorkspaceRole role
    );

    boolean existsByWorkspaceIdAndUserId(
            @Param("workspaceId") UUID workspaceId,
            @Param("userId") UUID userId
    );

    boolean existsByWorkspaceIdAndUserIdAndRoleIn(
            UUID workspaceId,
            UUID userId,
            Collection<WorkspaceRole> roles
    );

    int countByUserId(UUID userId);


}
