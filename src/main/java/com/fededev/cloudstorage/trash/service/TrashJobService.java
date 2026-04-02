package com.fededev.cloudstorage.trash.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.trash.model.TrashJob;
import com.fededev.cloudstorage.trash.model.TrashJobStatus;
import com.fededev.cloudstorage.trash.repository.TrashJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrashJobService {

    private final TrashJobRepository trashJobRepository;
    private final FileCleanupService fileCleanupService;

    public TrashJob create(UUID workspaceId, UUID requestBy){

        boolean exists = this.trashJobRepository.existsByWorkspaceIdAndStatusIn(
                workspaceId,
                List.of(TrashJobStatus.PENDING, TrashJobStatus.IN_PROGRESS)
        );

        if (exists){
            throw new AppException(ErrorCode.TRASH_JOB_ALREADY_EXISTS);
        }

        TrashJob trashJob = TrashJob.builder()
                .workspaceId(workspaceId)
                .requestedBy(requestBy)
                .status(TrashJobStatus.PENDING)
                .build();

        return this.trashJobRepository.save(trashJob);
    }

    @Scheduled(fixedDelay = 15000)
    public void processJobs(){

        // En producción, en este metodo conviene solo buscar jobs con estado PENDING
        // y buscar los jobs con estado FAILED en un cron job y ejecutar el borrado ahi
        List<TrashJob> jobs = this.trashJobRepository
                .findTop10ByStatusInOrderByCreatedAtAsc(List.of(TrashJobStatus.PENDING, TrashJobStatus.FAILED));

        for (TrashJob job : jobs) {
            try {
                job.markInProgress();
                this.trashJobRepository.save(job);

                processTrashJob(job.getWorkspaceId());

                job.markCompleted();
            } catch (Exception e) {
                job.markFailed(e.getMessage());
            }

            this.trashJobRepository.save(job);
        }

    }

    public void processTrashJob(UUID workspaceId){
        int batchSize = 500;

        while (true) {
            int deletedFiles = this.fileCleanupService.processBatch(workspaceId, batchSize);
            log.info("Eliminando archivos: {} encontrados", deletedFiles);
            if (deletedFiles == 0) {
                break;
            }
        }

        while (true) {
            int deletedFolders = this.fileCleanupService.processFolderBatch(workspaceId, batchSize);
            log.info("Eliminando carpetas: {} encontradas", deletedFolders);
            if (deletedFolders == 0) break;
        }

    }

}
