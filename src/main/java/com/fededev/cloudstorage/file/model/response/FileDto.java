package com.fededev.cloudstorage.file.model.response;

import com.fededev.cloudstorage.file.model.File;

import java.time.Instant;
import java.util.UUID;

public record FileDto(
        UUID id,
        String name,
        String extension,
        long size,
        String mimeType,
        UUID folderId,
        UUID workspaceId,
        UUID ownerId,
        Instant createdAt,
        Instant updatedAt
) {

    public static FileDto fromEntity(File file) {
        return new FileDto(
                file.getId(),
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