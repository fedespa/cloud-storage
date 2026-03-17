package com.fededev.cloudstorage.workspace.service;

import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.model.Workspace;
import com.fededev.cloudstorage.workspace.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.model.WorkspaceRole;
import com.fededev.cloudstorage.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Value("${app.workspace.default-quota}")
    private Long defaultQuota;

    public void createDefaultWorkspace(AppUser user) {

        Workspace workspace = Workspace.builder()
                .name("Mi Primer Workspace - " + user.getEmail().split("@")[0])
                .totalQuota(defaultQuota)
                .usedStorage(0L)
                .build();

        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .user(user)
                .role(WorkspaceRole.OWNER)
                .build();

        workspace.addMember(ownerMember);

        this.workspaceRepository.save(workspace);
    }

}
