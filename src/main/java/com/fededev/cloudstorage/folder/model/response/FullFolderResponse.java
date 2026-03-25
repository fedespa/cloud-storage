package com.fededev.cloudstorage.folder.model.response;

import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.response.FileDto;
import com.fededev.cloudstorage.folder.model.Folder;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

public record FullFolderResponse(
        UUID id,
        String name,
        UUID workspaceId,
        UUID parentId,
        Instant createdAt,
        Instant updatedAt,
        Page<FolderDto> children,
        Page<FileDto> files
) {

    public static FullFolderResponse fromEntity(Folder folder, Page<Folder> children, Page<File> files) {
        return new FullFolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getWorkspace() != null ? folder.getWorkspace().getId() : null,
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getCreatedAt(),
                folder.getUpdatedAt(),
                children.map(FolderDto::fromEntity),
                files.map(FileDto::fromEntity)
        );
    }

    public static FullFolderResponse forRoot(UUID workspaceId, Page<Folder> children, Page<File> files) {
        return new FullFolderResponse(
                workspaceId,
                "ROOT",
                workspaceId,
                null,
                null,
                null,
                children.map(FolderDto::fromEntity),
                files.map(FileDto::fromEntity)
        );
    }
}
