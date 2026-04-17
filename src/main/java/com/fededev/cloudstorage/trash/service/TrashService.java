package com.fededev.cloudstorage.trash.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.response.FileDto;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.model.response.FolderDto;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.trash.controller.response.TrashFolderResponse;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrashService {

    private final WorkspaceMemberService memberService;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final TrashJobService trashJobService;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TrashFolderResponse listContent(
            UUID workspaceId,
            UUID folderId,
            CustomUserDetails user,
            Pageable pageable
    ){
        validateListContentPermission(workspaceId, user.getId());

        if (folderId == null) {
            Page<Folder> rootFolders = this.folderRepository.findTrashRootFolders(workspaceId, pageable);
            Page<File> rootFiles = this.fileRepository.findTrashRootFiles(workspaceId, pageable);

            return new TrashFolderResponse(workspaceId, "Trash Bin", null,
                    rootFolders.map(FolderDto::fromEntity),
                    rootFiles.map(FileDto::fromEntity), true);
        }

        return listTrashFolder(folderId, pageable);
    }

    private TrashFolderResponse listTrashFolder(UUID folderId, Pageable pageable) {
        Folder folder = this.folderRepository.findDeletedById(folderId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

        Page<File> files = this.fileRepository.findTrashedFilesInFolder(folderId, pageable);
        Page<Folder> children = this.folderRepository.findTrashedSubfolders(folderId, pageable);

        return TrashFolderResponse.of(folder, children, files);
    }

    @PreAuthorize("isAuthenticated()")
    public void emptyTrash(UUID workspaceId, CustomUserDetails user){
        validateEmptyTrashPermission(workspaceId, user.getId());

        this.trashJobService.create(workspaceId, user.getId());
    }

    private void validateListContentPermission(UUID workspaceId, UUID userId){
        boolean hasAccess = this.memberService.isInWorkspace(workspaceId, userId);
        if (!hasAccess) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    private void validateEmptyTrashPermission(UUID workspaceId, UUID userId){
        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(workspaceId, userId);

        if (!member.isAdminOrOwner()) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

}
