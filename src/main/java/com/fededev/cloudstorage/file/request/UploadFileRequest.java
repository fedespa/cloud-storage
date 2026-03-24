package com.fededev.cloudstorage.file.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UploadFileRequest(

        UUID workspaceId,
        UUID folderId,
        MultipartFile file

) {
}
