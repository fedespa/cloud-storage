package com.fededev.cloudstorage.workspace.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.FileStatus;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.model.response.FullFolderResponse;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.model.response.MemberDto;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import com.fededev.cloudstorage.workspace.model.Workspace;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;
import com.fededev.cloudstorage.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceMemberService workspaceMemberService;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final WorkspaceRepository workspaceRepository;

    @Value("${app.workspace.default-quota}")
    private Long defaultQuota;

    public void createDefaultWorkspace(AppUser user) {
        Workspace workspace = Workspace.builder()
                .name("Mi Primer Workspace - " + user.getEmail().split("@")[0])
                .totalQuota(defaultQuota)
                .usedStorage(0L)
                .build();

        workspace.addMember(user, WorkspaceRole.OWNER);

        this.workspaceRepository.save(workspace);
    }

    public FullFolderResponse listRootContent(
            UUID workspaceId,
            Pageable folderPageable,
            Pageable filePageable,
            CustomUserDetails user
    ) {
        validateListContentPermission(workspaceId, user.getId());

        Page<Folder> children = this.folderRepository.findRootFolders(workspaceId, folderPageable);
        Page<File> files = this.fileRepository.findRootFiles(workspaceId, FileStatus.UPLOADED, filePageable);

        return FullFolderResponse.forRoot(workspaceId, children, files);
    }

    public List<MemberDto> listMembers(
            UUID workspaceId,
            CustomUserDetails user
    ){

        validateListMemberPermission(workspaceId, user.getId());

        List<WorkspaceMember> members = this.workspaceMemberService.getMembers(workspaceId);

        return members.stream()
                .map(MemberDto::from)
                .collect(Collectors.toList());

    }

    public void decreaseUsedStorage(UUID workspaceId, Long declaredSize) {
        this.workspaceRepository.decreaseUsedStorage(
                workspaceId,
                declaredSize
        );
    }

    public Workspace getById(UUID workspaceId) {
        return this.workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_NOT_FOUND));
    }

    private void validateListContentPermission(UUID workspaceId, UUID userId) {
        boolean hasAccess = this.workspaceMemberService.isInWorkspace(workspaceId, userId);
        if (!hasAccess) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    private void validateListMemberPermission(UUID workspaceId, UUID userId) {
        boolean hasAccess = this.workspaceMemberService.isInWorkspace(workspaceId, userId);

        if (!hasAccess) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

}
