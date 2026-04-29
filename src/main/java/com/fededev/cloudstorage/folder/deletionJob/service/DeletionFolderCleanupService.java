package com.fededev.cloudstorage.folder.deletionJob.service;

import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletionFolderCleanupService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFileBatch(List<UUID> fileIds) {
        this.fileRepository.softDeleteFilesByIds(fileIds);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFolderBatch(List<UUID> folderIds) {
        folderRepository.softDeleteFoldersByIds(folderIds);
    }

}
