package com.fededev.cloudstorage.trash.service;

import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.storage.StorageService;
import com.fededev.cloudstorage.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileCleanupService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final WorkspaceRepository workspaceRepository;
    private final StorageService storageService;

    @Transactional
    public int processBatch(UUID workspaceId, int batchSize) {
        List<File> files = this.fileRepository.findTrashedByWorkspace(
                workspaceId,
                PageRequest.of(0, batchSize)
        );

        if (files.isEmpty()) {
            return 0;
        }

        long totalSizeToRelease = files.stream().mapToLong(File::getSize).sum();

        List<String> keys = files.stream().map(File::getS3Key).toList();
        List<UUID> ids = files.stream().map(File::getId).toList();

        this.storageService.deleteFiles(keys);
        int deleted = this.fileRepository.deleteByIds(ids);

        this.workspaceRepository.decreaseUsedStorage(workspaceId, totalSizeToRelease);

        log.info("Batch eliminado: {} archivos de la DB", deleted);

        return deleted;
    }

    @Transactional
    public int processFolderBatch(UUID workspaceId, int batchSize){
        List<Folder> folders = this.folderRepository.findTrashedByWorkspace(
                workspaceId,
                PageRequest.of(0, batchSize)
        );

        if (folders.isEmpty()) return 0;

        List<UUID> ids = folders.stream().map(Folder::getId).toList();
        return this.folderRepository.deleteByIds(ids);
    }

}
