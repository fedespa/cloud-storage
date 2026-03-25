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

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final WorkspaceMemberService workspaceMemberService;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public FolderDto create(UUID workspaceId, CreateFolderRequest request, CustomUserDetails userDetails){
        UUID userId = userDetails.getId();

        WorkspaceMember workspaceMember = this.workspaceMemberService.getMemberIfIsInWorkspace(workspaceId, userId);

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
                .name(request.name())
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

    public Folder getByIdAndWorkspace(UUID folderId, UUID workspaceId) {
        return this.folderRepository.findByIdAndWorkspaceId(folderId, workspaceId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND));
    }

    private void validateExistingFolder(UUID parentId, UUID workspaceId){
        boolean isValid = this.folderRepository.existsByIdAndWorkspace(parentId, workspaceId);

        if (!isValid) {
            throw new AppException(ErrorCode.FOLDER_NOT_FOUND);
        }
    }

    private void validateFolderNameUniqueness(String folderName, UUID workspaceId, UUID parentId){
        boolean exists = this.folderRepository.existsByNameInLevel(folderName.trim(), workspaceId, parentId);

        if (exists) {
            throw new AppException(ErrorCode.FOLDER_ALREADY_EXISTS_IN_LEVEL);
        }
    }

}
