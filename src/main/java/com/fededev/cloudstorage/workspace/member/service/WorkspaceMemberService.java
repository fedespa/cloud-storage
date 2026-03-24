package com.fededev.cloudstorage.workspace.member.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;
import com.fededev.cloudstorage.workspace.member.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository memberRepository;

    public boolean isInWorkspace(UUID workspaceId, UUID userId) {
        return this.memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }

    public WorkspaceMember getMemberWithWorkspace(UUID workspaceId, UUID userId) {
        return this.memberRepository.findWithWorkspace(workspaceId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND));
    }

    public boolean hasRole(UUID workspaceId, UUID userId, WorkspaceRole role) {
        return this.memberRepository.existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId, role);
    }

    public boolean hasAnyRole(UUID workspaceId, UUID userId, List<WorkspaceRole> roles) {
        return this.memberRepository.existsByWorkspaceIdAndUserIdAndRoleIn(workspaceId, userId, roles);
    }

    public boolean isOwner(UUID workspaceId, UUID userId) {
        return hasRole(workspaceId, userId, WorkspaceRole.OWNER);
    }

    public boolean isAdmin(UUID workspaceId, UUID userId) {
        return hasRole(workspaceId, userId, WorkspaceRole.ADMIN);
    }

    public boolean isMember(UUID workspaceId, UUID userId) {
        return this.memberRepository.existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId,  WorkspaceRole.MEMBER);
    }

    public boolean isViewer(UUID workspaceId, UUID userId) {
        return hasRole(workspaceId, userId, WorkspaceRole.VIEWER);
    }

    public boolean isAdminOrOwner(UUID workspaceId, UUID userId) {
        return hasAnyRole(workspaceId, userId, List.of(
                WorkspaceRole.OWNER,
                WorkspaceRole.ADMIN
        ));
    }

    public void validateIsInWorkspace(UUID workspaceId, UUID userId) {
        boolean isMember = isInWorkspace(workspaceId, userId);

        if (!isMember) {
            throw new AppException(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND);
        }
    }

    public WorkspaceMember getMemberIfIsInWorkspace(UUID workspaceId, UUID userId) {
        return this.memberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND));
    }

    public void validateWorkspaceLimit(UUID userId) {
        int count = this.memberRepository.countByUserId(userId);

        if (count >= 5) {
            throw new AppException(ErrorCode.WORKSPACE_LIMIT_REACHED);
        }
    }

}
