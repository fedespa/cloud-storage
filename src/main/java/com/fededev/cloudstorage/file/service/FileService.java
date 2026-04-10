package com.fededev.cloudstorage.file.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.response.FileDto;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.file.request.MoveFileRequest;
import com.fededev.cloudstorage.file.request.UploadFileRequest;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.storage.StorageService;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.repository.UserRepository;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import com.fededev.cloudstorage.workspace.model.Workspace;
import com.fededev.cloudstorage.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final WorkspaceMemberService memberService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final WorkspaceRepository workspaceRepository;

    private final Set<String> allowedExtensions = Set.of("jpg", "jpeg", "png", "pdf", "docx");

    @Value("${app.storage.max-file-size}")
    private long maxFileSize;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public FileDto uploadFile(
            UploadFileRequest request,
            CustomUserDetails user
    ) {
        MultipartFile file = request.file();
        validateFile(file);

        String fullFilename = Objects.requireNonNull(file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(fullFilename); // "jpg"

        String nameWithoutExtension = StringUtils.stripFilenameExtension(fullFilename);
        String sanitizedName = sanitizeFilename(nameWithoutExtension);

        Folder parent = null;
        UUID workspaceId;

        if (request.folderId() != null) {
            parent = this.folderRepository.findActiveById(request.folderId())
                    .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

            workspaceId = parent.getWorkspace().getId();

            if (request.workspaceId() != null && !workspaceId.equals(request.workspaceId())) {
                throw new AppException(ErrorCode.FOLDER_NOT_BELONG_TO_WORKSPACE);
            }
        } else {
            if (request.workspaceId() == null) {
                throw new AppException(ErrorCode.WORKSPACE_REQUIRED);
            }
            workspaceId = request.workspaceId();
        }

        WorkspaceMember member = this.memberService.getMemberWithWorkspace(workspaceId, user.getId());
        if (!member.canUpload()){
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        };

        Workspace workspace = member.getWorkspace();

        int updated = this.workspaceRepository.increaseUsedStorageIfPossible(workspaceId, file.getSize());

        if (updated == 0) {
            throw new AppException(ErrorCode.WORKSPACE_QUOTA_EXCEEDED);
        }

        String s3Key = generateS3Key(workspace.getId(), sanitizedName);

        AppUser owner = this.userRepository.getReferenceById(user.getId());

        boolean alreadyExists = this.fileRepository.existsByNameAndContext(
                sanitizedName,
                extension,
                workspace.getId(),
                parent != null ? parent.getId() : null
        );

        if (alreadyExists) {
            throw new AppException(ErrorCode.DUPLICATE_FILE_NAME);
        }

        try (InputStream inputStream = file.getInputStream()) {
            this.storageService.upload(s3Key, inputStream, file.getContentType(), file.getSize());

            File newFile = File.builder()
                    .name(sanitizedName)
                    .extension(extension)
                    .folder(parent)
                    .owner(owner)
                    .workspace(workspace)
                    .size(file.getSize())
                    .s3Key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            File savedFile = this.fileRepository.save(newFile);
            return FileDto.fromEntity(savedFile);
        } catch (Exception e) {
            this.storageService.delete(s3Key);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

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

    private String generateS3Key(UUID workspaceId, String sanitizedName) {
        return String.format("workspaces/%s/%s-%s",
                workspaceId, UUID.randomUUID(), sanitizedName);
    }

    private void validateFile(MultipartFile file){
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        if (file.getSize() > this.maxFileSize) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (extension == null || !allowedExtensions.contains(extension.toLowerCase())) {
            throw new AppException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        if (Set.of("jpg", "jpeg", "png").contains(extension.toLowerCase())) {
            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                throw new AppException(ErrorCode.INVALID_FILE_TYPE);
            }
        }
    }

    private String sanitizeFilename(String filename) {
        return filename
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
    }
}
