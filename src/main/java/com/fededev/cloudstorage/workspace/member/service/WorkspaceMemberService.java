package com.fededev.cloudstorage.workspace.member.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;
import com.fededev.cloudstorage.workspace.member.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public boolean isOwner(UUID workspaceId, UUID userId) {
        return this.workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId, WorkspaceRole.OWNER);
    }

    public boolean isMember(UUID workspaceId, UUID userId) {
        return this.workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }

    public boolean isAdmin(UUID workspaceId, UUID userId) {
        return this.workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId, WorkspaceRole.ADMIN);
    }

    public boolean isAdminOrOwner(UUID workspaceId, UUID userId) {
        return this.workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndRoleIn(workspaceId, userId, List.of(WorkspaceRole.OWNER, WorkspaceRole.ADMIN));
    }

    public void validateWorkspaceLimit(UUID userId) {
        int count = this.workspaceMemberRepository.countByUserId(userId);

        if (count >= 5) {
            throw new AppException(ErrorCode.WORKSPACE_LIMIT_REACHED);
        }
    }

}
