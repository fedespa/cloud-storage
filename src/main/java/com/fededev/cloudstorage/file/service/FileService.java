package com.fededev.cloudstorage.file.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.FileStatus;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.file.request.MoveFileRequest;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.storage.StorageService;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
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
        File file = getActiveFileOrThrow(fileId);

        validateDownloadPermission(file.getWorkspace().getId(), userId);

        return this.storageService.generateUrl(file.getS3Key(), file.getName(), file.getExtension());
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void moveFile(
            UUID fileId,
            MoveFileRequest request,
            CustomUserDetails user
    ){
        File file = getActiveFileOrThrow(fileId);

        UUID workspaceId = file.getWorkspace().getId();

        validateMovePermission(workspaceId, file, user.getId());

        UUID currentFolderId = file.getFolder() != null ? file.getFolder().getId() : null;

        Folder folderDestination = resolveFolderDestination(workspaceId, request.targetFolderId(), currentFolderId);

        file.changeFolder(folderDestination);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void softDelete(UUID fileId, CustomUserDetails user) {
        File file = getActiveFileOrThrow(fileId);

        if (file.getDeletedAt() != null) {
            return;
        }

        validateDeletePermission(file.getWorkspace().getId(), file, user.getId());

        file.markAsDeleted();
    }

    public Page<File> findActiveFilesInFolder(UUID folderId, Pageable pageable){
        return this.fileRepository.findActiveFilesInFolder(folderId, FileStatus.UPLOADED, pageable);
    }

    private File getActiveFileOrThrow(UUID fileId) {
        return this.fileRepository
                .findActiveByIdWithWorkspace(fileId, FileStatus.UPLOADED)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
    }

    private void validateDownloadPermission(UUID workspaceId, UUID userId){
        boolean isMember = this.memberService.isInWorkspace(workspaceId, userId);

        if (!isMember) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    private void validateDeletePermission(UUID workspaceId, File file, UUID userId) {
        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(workspaceId, userId);

        if (!member.isAdminOrOwner() && !file.isOwnerOfFile(userId)) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    private Folder resolveFolderDestination(UUID workspaceId, UUID targetFolderId, UUID currentFolderId){
        if (Objects.equals(currentFolderId, targetFolderId)) {
            throw new AppException(ErrorCode.FILE_ALREADY_IN_FOLDER);
        }

        if (targetFolderId != null) {
            return this.folderRepository.findActiveForUpdate(targetFolderId, workspaceId)
                    .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));
        }

        return null;
    }

    private void validateMovePermission(UUID workspaceId, File file, UUID userId){
        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(workspaceId, userId);

        if (!member.isAdminOrOwner() && !file.isOwnerOfFile(userId)) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }
}
