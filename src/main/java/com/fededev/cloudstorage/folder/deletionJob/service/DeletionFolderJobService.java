package com.fededev.cloudstorage.folder.deletionJob.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.folder.deletionJob.model.DeletionFolderJob;
import com.fededev.cloudstorage.folder.deletionJob.model.DeletionFolderJobStatus;
import com.fededev.cloudstorage.folder.deletionJob.repository.DeletionFolderJobRepository;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeletionFolderJobService {

    private final DeletionFolderJobRepository deletionFolderJobRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final DeletionFolderCleanupService deletionFolderCleanupService;
    private static final int MAX_ATTEMPTS = 3;

    public DeletionFolderJob create(UUID folderId, UUID requestedBy){

        boolean exists = this.deletionFolderJobRepository.existsByFolderId(folderId);

        if (exists){
            throw new AppException(ErrorCode.DELETION_FOLDER_JOB_ALREADY_EXISTS);
        }

        DeletionFolderJob newJob = DeletionFolderJob.builder()
                .folderId(folderId)
                .requestedBy(requestedBy)
                .status(DeletionFolderJobStatus.PENDING)
                .build();

        return this.deletionFolderJobRepository.save(newJob);
    }

    @Scheduled(fixedDelay = 60000)
    public void processJobs(){
        Instant oneHourAgo = Instant.now().minus(10, ChronoUnit.MINUTES);
        List<DeletionFolderJob> jobs = this.deletionFolderJobRepository.findJobsToProcess(oneHourAgo);

        for (DeletionFolderJob job : jobs){

            if (job.getAttempts() >= MAX_ATTEMPTS){
                job.markFailed("Superó el límite de intentos");
                this.deletionFolderJobRepository.save(job);
                continue;
            }

            job.markInProgress();
            job.increaseAttempts();
            this.deletionFolderJobRepository.save(job);

            try {
                processJob(job);
                job.markCompleted();
            } catch (Exception e) {
                log.error("[Job Deletion] Falló el intento {} para la carpeta {}. Causa: {}",
                        job.getAttempts(), job.getFolderId(), e.getMessage(), e);
                job.markPending();
            } finally {
                this.deletionFolderJobRepository.save(job);
            }
        }

    }

    public void processJob(DeletionFolderJob job){
        int batchSize = 500;
        boolean filesRemaining = true;
        boolean foldersRemaining = true;

        while (filesRemaining) {
            List<UUID> fileIds = this.fileRepository.findFileIdsToDeleteInBatch(job.getFolderId(), batchSize);

            if (fileIds.isEmpty()) {
                filesRemaining = false;
            } else {
                this.deletionFolderCleanupService.deleteFileBatch(fileIds);
            }
        }

        while (foldersRemaining) {
            List<UUID> folderIds = this.folderRepository.findFolderIdsToDeleteInBatch(job.getFolderId(), batchSize);

            if (folderIds.isEmpty()) {
                foldersRemaining = false;
            }  else {
                this.deletionFolderCleanupService.deleteFolderBatch(folderIds);
            }
        }

    }
}
