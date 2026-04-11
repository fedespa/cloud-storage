package com.fededev.cloudstorage.file.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.file.request.MoveFileRequest;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.storage.StorageService;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final WorkspaceMemberService memberService;
    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    @PreAuthorize("isAuthenticated()")
    public String generatePresignedUrl(UUID fileId, UUID userId) {

        File file = this.fileRepository.findActiveByIdWithWorkspace(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        boolean isMember = this.memberService.isInWorkspace(file.getWorkspace().getId(), userId);

        if (!isMember) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        String url = this.storageService.generateUrl(file.getS3Key(), file.getName(), file.getExtension());

        return url;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void moveFile(
            UUID fileId,
            MoveFileRequest request,
            CustomUserDetails user
    ){
        File file = this.fileRepository.findActiveByIdWithWorkspace(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        UUID workspaceId = file.getWorkspace().getId();

        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(workspaceId, user.getId());

        if (!member.isAdminOrOwner() && !file.isOwnerOfFile(user.getId())) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        Folder folderDestination = null;

        if (request.folderDestinationId() != null) {
            folderDestination = this.folderRepository.findByIdAndWorkspaceId(request.folderDestinationId(), workspaceId)
                    .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));
        }

        file.changeFolder(folderDestination);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void softDelete(UUID fileId, CustomUserDetails user) {
        File file = this.fileRepository.findActiveByIdWithWorkspace(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(file.getWorkspace().getId(), user.getId());

        if (!member.isAdminOrOwner() && !file.isOwnerOfFile(user.getId())) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        if (file.getDeletedAt() != null) {
            return;
        }

        file.markAsDeleted();
    }
}
