package com.fededev.cloudstorage.file.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.FileStatus;
import com.fededev.cloudstorage.file.model.response.FileConfirmResponse;
import com.fededev.cloudstorage.file.model.response.InitUploadResponseDto;
import com.fededev.cloudstorage.file.model.response.UploadContext;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.file.request.UploadFileRequest;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.folder.service.FolderService;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.infraestructure.validator.FileTypeValidator;
import com.fededev.cloudstorage.storage.StorageService;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import com.fededev.cloudstorage.workspace.model.Workspace;
import com.fededev.cloudstorage.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadFileService {

    private final WorkspaceMemberService memberService;
    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final FileTypeValidator validator;
    private final TransactionTemplate transactionTemplate;
    private final DatabaseUploadService databaseUploadService;
    private final WorkspaceService workspaceService;
    private final FolderService folderService;

    @Value("${app.storage.max-file-size}")
    private long maxSizeBytes;

    @PreAuthorize("isAuthenticated()")
    public InitUploadResponseDto initiateUpload(
            UUID workspaceId,
            UUID folderId,
            UploadFileRequest request,
            CustomUserDetails user
    ){
        validateUploadRequest(workspaceId, folderId, request);

        String ext = extractExtension(request.filename());

        String sanitizedName = sanitizeFilename(
                StringUtils.stripFilenameExtension(request.filename())
        );

        UploadContext context = resolveUploadContext(workspaceId, folderId, request);

        Workspace workspace = context.workspace();
        Folder folder = context.folder();

        validateUploadPermission(context.workspace().getId(), user.getId());

        validateDuplicateFile(sanitizedName, ext, workspace.getId(), folder);

        String s3key = generateS3Key(context.workspace().getId(), sanitizedName, ext);

        File file = this.databaseUploadService.reserveQuotaAndCreatePendingFile(
                sanitizedName,
                ext,
                request,
                context,
                user,
                s3key,
                workspace.getId(),
                request.sizeBytes()
        );

        Map<String, String> fields = this.storageService.generatePresignedPost(
                file.getS3Key(),
                request.contentType(),
                request.sizeBytes()
        );

        String uploadUrl = fields.remove("upload_url");

        return new InitUploadResponseDto(file.getId(), uploadUrl, fields);
    }

    public FileConfirmResponse confirm(
            UUID fileId,
            CustomUserDetails user
    ) {

        File file = this.fileRepository.findByIdAndOwnerId(
                fileId,
                user.getId()
        ).orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        if (file.getStatus().equals(FileStatus.UPLOADED)) {
            return new FileConfirmResponse(file.getId(), "uploaded");
        }
        if (file.getStatus() != FileStatus.PENDING) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }

        HeadObjectResponse head;
        try {
            head = this.storageService.headObject(file.getS3Key());
        } catch (Exception e) {
            file.markAsFailed();
            this.fileRepository.save(file);
            throw e;
        }

        long actualSize = head.contentLength();

        if (actualSize == 0) {
            file.markAsFailed();
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }

        long declaredSize = file.getSize();
        file.setSize(actualSize);

        try {
            this.validator.validateMagicBytes(
                    storageService.getClient(),
                    storageService.getBucket(),
                    file.getS3Key(),
                    file.getMimeType()
            );
        } catch (Exception e) {
            this.storageService.delete(file.getS3Key());

            this.transactionTemplate.executeWithoutResult(status -> {
                file.markAsFailed();
                this.workspaceService.decreaseUsedStorage(
                        file.getWorkspace().getId(),
                        declaredSize
                );
                this.fileRepository.save(file);
            });

            throw e;
        }

        String permanentKey = this.storageService.promoteObject(file.getS3Key());

        this.transactionTemplate.executeWithoutResult(status -> {
            file.setS3Key(permanentKey);
            file.markAsUploaded();
            this.fileRepository.save(file);
        });

        return new FileConfirmResponse(file.getId(), "uploaded");
    }

    @Transactional
    public void confirmError(
            Map<String, String> body,
            CustomUserDetails user
    ) {
        this.fileRepository.findByIdAndOwnerIdAndStatus(
                UUID.fromString(body.get("fileId")),
                user.getId(),
                FileStatus.PENDING
        ).ifPresent(file -> {
            file.setStatus(FileStatus.FAILED);
            this.workspaceService.decreaseUsedStorage(file.getWorkspace().getId(), file.getSize());
            this.fileRepository.save(file);
        });
    }

    private void validateDuplicateFile(String sanitizedName, String ext, UUID workspaceId, Folder folder){
        boolean alreadyExists = this.fileRepository.existsByNameAndContext(
                sanitizedName,
                ext,
                workspaceId,
                folder != null ? folder.getId() : null
        );

        if (alreadyExists) {
            throw new AppException(ErrorCode.DUPLICATE_FILE_NAME);
        }
    }

    private void validateUploadPermission(UUID workspaceId, UUID userId){
        WorkspaceMember member = this.memberService.getMemberWithWorkspace(workspaceId, userId);
        if (!member.canUpload()){
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        };
    }

    private void validateUploadRequest(UUID workspaceId, UUID folderId, UploadFileRequest request){
        if (folderId == null && workspaceId == null){
            throw new AppException(ErrorCode.MUST_SEND_WORKSPACE_OR_FOLDER);
        }

        this.validator.validateMimeType(request.contentType());

        if (request.sizeBytes() > maxSizeBytes || request.sizeBytes() <= 0) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    private UploadContext resolveUploadContext(UUID workspaceId, UUID folderId, UploadFileRequest request){
        Folder folder = null;
        UUID finalWorkspaceId;

        if (folderId != null) {
            folder = this.folderService.getActiveById(folderId);

            finalWorkspaceId = folder.getWorkspace().getId();
        } else {
            if (workspaceId == null) {
                throw new AppException(ErrorCode.WORKSPACE_REQUIRED);
            }
            finalWorkspaceId = workspaceId;
        }

        Workspace workspace = this.workspaceService.getById(finalWorkspaceId);

        return new UploadContext(workspace, folder);
    }

    private String generateS3Key(UUID workspaceId, String sanitizedName, String ext) {
        return String.format("workspaces/%s/%s-%s.%s",
                workspaceId, UUID.randomUUID(), sanitizedName, ext);
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "bin";
        return filename.substring(dot + 1).toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

    private String sanitizeFilename(String filename) {
        return filename
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
    }

}
