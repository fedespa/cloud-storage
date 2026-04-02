package com.fededev.cloudstorage.folder.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.repository.FileRepository;
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
    private final FileRepository fileRepository;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public FolderDto create(UUID workspaceId, CreateFolderRequest request, CustomUserDetails userDetails){
        UUID userId = userDetails.getId();

        WorkspaceMember workspaceMember = this.memberService.getMemberIfIsInWorkspace(workspaceId, userId);

        if (!workspaceMember.canUpload()) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        this.validateFolderNameUniqueness(request.name(), workspaceId, request.parentId());

        Folder parent = null;
        if (request.parentId() != null) {
            validateExistingFolder(request.parentId(), workspaceId);
            parent = this.folderRepository.getReferenceById(request.parentId());
        }

        AppUser owner = this.userRepository.getReferenceById(userId);
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
            CustomUserDetails userDetails
    ){
        UUID userId = userDetails.getId();

        Folder folder = this.folderRepository.findByIdAndUserAccess(folderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

        Page<Folder> children = this.folderRepository.findByParentIdAndDeletedAtIsNull(folderId, childrenPageable);
        Page<File> files = this.fileRepository.findByFolderIdAndDeletedAtIsNull(folderId, filesPageable);

        return FullFolderResponse.fromEntity(folder, children, files);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public FolderDto move(
            UUID folderId,
            MoveFolderRequest request,
            CustomUserDetails user
    ) {

        if (folderId.equals(request.destinationId())) {
            throw new AppException(ErrorCode.CANNOT_MOVE_FOLDER_INTO_ITSELF);
        }

        Folder folderToMove = this.folderRepository.findActiveByIdWithWorkspace(folderId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(
                folderToMove.getWorkspace().getId(),
                user.getId()
        );

        if (!member.isAdminOrOwner()) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        Folder folderDestination = null;

        if (request.destinationId() != null) {
            folderDestination = this.folderRepository.findActiveByIdWithWorkspace(request.destinationId())
                    .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

            if (!folderDestination.getWorkspace().getId().equals(folderToMove.getWorkspace().getId())) {
                throw new AppException(ErrorCode.FOLDER_NOT_BELONG_TO_WORKSPACE);
            }

            long isSubfolder = this.folderRepository.isDescendant(request.destinationId(), folderToMove.getId());
            if (isSubfolder > 0) {
                throw new AppException(ErrorCode.CANNOT_MOVE_INTO_SUBFOLDER);
            }
        }

        folderToMove.moveTo(folderDestination);

        return FolderDto.fromEntity(folderToMove);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void delete(
            UUID folderId,
            CustomUserDetails user
    ){

        Folder folder = this.folderRepository.findActiveById(folderId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

        if (folder.getDeletedAt() != null) {
            throw new AppException(ErrorCode.FOLDER_ALREADY_DELETED);
        }

        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(folder.getWorkspace().getId(), user.getId());

        if (!member.isAdminOrOwner() && !folder.isOwnerOfFolder(user.getId())) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        this.fileRepository.softDeleteFilesInFolders(folder.getId());
        this.folderRepository.softDeleteFolderAndSubfolders(folder.getId());
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
