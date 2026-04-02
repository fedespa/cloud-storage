package com.fededev.cloudstorage.trash.controller.response;

import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.response.FileDto;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.model.response.FolderDto;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

public record TrashFolderResponse(
        UUID id,
        String name,
        Instant deletedAt,
        Page<FolderDto> children,
        Page<FileDto> files,
        boolean isRoot
) {

    public static TrashFolderResponse of(Folder folder, Page<Folder> children, Page<File> files) {
        return new TrashFolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getDeletedAt(),
                children.map(FolderDto::fromEntity),
                files.map(FileDto::fromEntity),
                false
        );
    }
}
