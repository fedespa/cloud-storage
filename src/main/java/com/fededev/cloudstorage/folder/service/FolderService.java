package com.fededev.cloudstorage.folder.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.service.FileService;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.model.response.FolderDto;
import com.fededev.cloudstorage.folder.model.response.FullFolderResponse;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.folder.request.CreateFolderRequest;
import com.fededev.cloudstorage.folder.request.MoveFolderRequest;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.repository.UserRepository;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import com.fededev.cloudstorage.workspace.model.Workspace;
import com.fededev.cloudstorage.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final WorkspaceMemberService memberService;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FolderRepository folderRepository;
    private final FileService fileService;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public FolderDto create(UUID workspaceId, CreateFolderRequest request, CustomUserDetails user){
        validateUploadPermission(workspaceId, user.getId());

        validateFolderNameUniqueness(request.name(), workspaceId, request.parentId());

        Folder parent = resolveParentFolder(request.parentId(), workspaceId);

        AppUser owner = this.userRepository.getReferenceById(user.getId());
        Workspace workspace = this.workspaceRepository.getReferenceById(workspaceId);

        Folder folder = Folder.builder()
                .name(request.name().trim().toLowerCase())
                .owner(owner)
                .workspace(workspace)
                .parent(parent)
                .build();

        Folder savedFolder = this.folderRepository.save(folder);

        return FolderDto.fromEntity(savedFolder);
    }

    public FullFolderResponse listFolder(
            UUID folderId,
            Pageable childrenPageable,
            Pageable filesPageable,
            CustomUserDetails user
    ){
        Folder folder = this.folderRepository.findByIdAndUserAccess(folderId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

        Page<Folder> children = this.folderRepository.findByParentIdAndDeletedAtIsNull(folderId, childrenPageable);
        Page<File> files = this.fileService.findActiveFilesInFolder(folderId, filesPageable);

        return FullFolderResponse.fromEntity(folder, children, files);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public FolderDto move(UUID folderId, MoveFolderRequest request,CustomUserDetails user) {

        if (folderId.equals(request.targetFolderId())) {
            throw new AppException(ErrorCode.CANNOT_MOVE_FOLDER_INTO_ITSELF);
        }

        Folder folderToMove = this.folderRepository.findActiveByIdWithWorkspace(folderId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

        validateMovePermission(folderToMove.getWorkspace().getId(), user.getId());

        Folder folderDestination = resolveTargetFolder(
                request.targetFolderId(),
                folderToMove.getId(),
                folderToMove.getWorkspace().getId()
        );

        folderToMove.moveTo(folderDestination);

        return FolderDto.fromEntity(folderToMove);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void delete(UUID folderId, CustomUserDetails user) {
        Folder folder = this.folderRepository.findActiveById(folderId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

        if (folder.getDeletedAt() != null) {
            throw new AppException(ErrorCode.FOLDER_ALREADY_DELETED);
        }

        validateDeletePermission(folder, user.getId());


        this.fileService.softDeleteAllUnderFolder(folder.getId());
        this.folderRepository.softDeleteFolderAndSubfolders(folder.getId());
    }

    public Folder getActiveById(UUID folderId) {
        return this.folderRepository.findActiveById(folderId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));
    }

    private void validateDeletePermission(Folder folder, UUID userId) {
        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(folder.getWorkspace().getId(), userId);

        if (!member.isAdminOrOwner() && !folder.isOwnerOfFolder(userId)) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    private Folder resolveTargetFolder(UUID targetFolderId, UUID folderToMoveId, UUID workspaceId) {
        if (targetFolderId != null) {
            Folder folderDestination = this.folderRepository.findActiveByIdWithWorkspace(targetFolderId)
                    .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

            if (!folderDestination.getWorkspace().getId().equals(workspaceId)) {
                throw new AppException(ErrorCode.FOLDER_NOT_BELONG_TO_WORKSPACE);
            }

            long isSubfolder = this.folderRepository.isDescendant(targetFolderId, folderToMoveId);
            if (isSubfolder > 0) {
                throw new AppException(ErrorCode.CANNOT_MOVE_INTO_SUBFOLDER);
            }

            return folderDestination;
        }

        return null;
    }

    private void validateMovePermission(UUID workspaceID, UUID userId){
        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(
                workspaceID,
                userId
        );

        if (!member.isAdminOrOwner()) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    private Folder resolveParentFolder(UUID parentId, UUID workspaceId){
        if (parentId != null) {
            validateExistingFolder(parentId, workspaceId);
            return this.folderRepository.getReferenceById(parentId);
        }

        return null;
    }

    private void validateUploadPermission(UUID workspaceId, UUID userId){
        WorkspaceMember workspaceMember = this.memberService.getMemberIfIsInWorkspace(workspaceId, userId);

        if (!workspaceMember.canUpload()) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    private void validateExistingFolder(UUID parentId, UUID workspaceId){
        boolean isValid = this.folderRepository.existsByIdAndWorkspace(parentId, workspaceId);

        if (!isValid) {
            throw new AppException(ErrorCode.FOLDER_NOT_FOUND);
        }
    }

    private void validateFolderNameUniqueness(String folderName, UUID workspaceId, UUID parentId){
        boolean exists = this.folderRepository.existsByNameInLevel(folderName.trim().toLowerCase(), workspaceId, parentId);

        if (exists) {
            throw new AppException(ErrorCode.FOLDER_ALREADY_EXISTS_IN_LEVEL);
        }
    }

}