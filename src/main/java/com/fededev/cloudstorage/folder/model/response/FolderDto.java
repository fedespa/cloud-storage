package com.fededev.cloudstorage.folder.model.response;

import com.fededev.cloudstorage.folder.model.Folder;

import java.time.Instant;
import java.util.UUID;

public record FolderDto(
    UUID id,
    String name,
    UUID workspaceId,
    UUID parentId,
    Instant createdAt,
    Instant updatedAt
) {

    public static FolderDto fromEntity(Folder folder) {
        return new FolderDto(
                folder.getId(),
                folder.getName(),
                folder.getWorkspace() != null ? folder.getWorkspace().getId() : null,
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }

}
