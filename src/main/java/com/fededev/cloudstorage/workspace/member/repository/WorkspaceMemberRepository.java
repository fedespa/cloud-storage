package com.fededev.cloudstorage.workspace.member.repository;

import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(
            @Param("workspaceId") UUID workspaceId,
            @Param("userId") UUID userId
    );

    @Query("""
        SELECT wm FROM WorkspaceMember wm
        JOIN FETCH wm.workspace
        WHERE wm.workspace.id = :workspaceId
        AND wm.user.id = :userId
    """)
    Optional<WorkspaceMember> findWithWorkspace(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);

    boolean existsByWorkspaceIdAndUserIdAndRoleIn(
            UUID workspaceId,
            UUID userId,
            Collection<WorkspaceRole> roles
    );

    int countByUserId(UUID userId);

    @Query("""
        SELECT m FROM WorkspaceMember m
        JOIN FETCH m.user u
        WHERE m.workspace.id = :workspaceId
    """)
    List<WorkspaceMember> getMembers(@Param("workspaceId") UUID workspaceId);


}
