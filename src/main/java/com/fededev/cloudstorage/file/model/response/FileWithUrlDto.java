package com.fededev.cloudstorage.file.model.response;

import com.fededev.cloudstorage.file.model.File;

import java.time.Instant;
import java.util.UUID;

public record FileWithUrlDto(
        UUID id,
        String url,
        String name,
        String extension,
        long size,
        String contentType,
        UUID folderId,
        UUID workspaceId,
        UUID ownerId,
        Instant createdAt,
        Instant updatedAt
) {

    public static FileWithUrlDto fromEntity(File file, String url) {
        return new FileWithUrlDto(
                file.getId(),
                url,
                file.getName(),
                file.getExtension(),
                file.getSize(),
                file.getMimeType(),
                file.getFolder() != null ? file.getFolder().getId() : null,
                file.getWorkspace().getId(),
                file.getOwner().getId(),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }

}
