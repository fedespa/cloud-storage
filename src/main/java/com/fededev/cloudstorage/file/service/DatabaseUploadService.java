package com.fededev.cloudstorage.file.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.FileStatus;
import com.fededev.cloudstorage.file.model.response.UploadContext;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.file.request.UploadFileRequest;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.repository.UserRepository;
import com.fededev.cloudstorage.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DatabaseUploadService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    @Transactional
    public File reserveQuotaAndCreatePendingFile(
            String sanitizedName,
            String ext,
            UploadFileRequest request,
            UploadContext context,
            CustomUserDetails user,
            String s3Key,
            UUID workspaceId,
            Long sizeBytes
    ) {

        AppUser owner = this.userRepository.getReferenceById(user.getId());

        File newFile = File.builder()
                .name(sanitizedName)
                .extension(ext)
                .folder(context.folder())
                .owner(owner)
                .workspace(context.workspace())
                .size(request.sizeBytes())
                .s3Key(s3Key)
                .mimeType(request.contentType())
                .status(FileStatus.PENDING)
                .build();

        reserveQuota(workspaceId, sizeBytes);

        return this.fileRepository.save(newFile);
    }

    private void reserveQuota(UUID workspaceId, Long sizeBytes){
        int updated = this.workspaceRepository.increaseUsedStorageIfPossible(workspaceId, sizeBytes);

        if (updated == 0) {
            throw new AppException(ErrorCode.WORKSPACE_QUOTA_EXCEEDED);
        }
    }

}
