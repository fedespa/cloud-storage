package com.fededev.cloudstorage.file.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.response.FileDto;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.file.request.UploadFileRequest;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.service.FolderService;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.storage.StorageService;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.repository.UserRepository;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import com.fededev.cloudstorage.workspace.model.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final WorkspaceMemberService memberService;
    private final FolderService folderService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

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

        String contentType = file.getContentType();
        long fileSize = file.getSize();
        String originalName = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalName);

        UUID workspaceId = request.workspaceId();
        UUID folderId = request.folderId();

        WorkspaceMember member = this.memberService.getMemberWithWorkspace(workspaceId, user.getId());

        if (!member.canUpload()){
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        };

        Workspace workspace = member.getWorkspace();
        workspace.consumeStorage(fileSize);

        Folder folder = null;
        if (folderId != null) {
             folder = this.folderService.getByIdAndWorkspace(folderId, workspaceId);
        }

        String sanitizedName = sanitizeFilename(originalName);

        String s3Key = String.format("workspaces/%s/%s-%s",
                workspaceId, UUID.randomUUID(), sanitizedName);

        AppUser owner = this.userRepository.getReferenceById(user.getId());

        try (InputStream inputStream = file.getInputStream()) {
            this.storageService.upload(s3Key, inputStream, contentType, fileSize);

            File newFile = File.builder()
                    .name(originalName)
                    .extension(extension)
                    .folder(folder)
                    .owner(owner)
                    .workspace(workspace)
                    .size(fileSize)
                    .s3Key(s3Key)
                    .contentType(contentType)
                    .build();

            this.fileRepository.save(newFile);
            return FileDto.fromEntity(newFile);
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_READ_ERROR);
        } catch (Exception e) {
            this.storageService.delete(s3Key);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
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
        return filename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }
}
