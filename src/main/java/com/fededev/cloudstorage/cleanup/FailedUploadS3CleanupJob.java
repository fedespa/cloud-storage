package com.fededev.cloudstorage.cleanup;

import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class FailedUploadS3CleanupJob {

    private final FileRepository fileRepository;
    private final StorageService storageService;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    @Transactional
    public void deleteFailedObjects() {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.HOURS);

        List<File> failed = this.fileRepository.findFailed(cutoff);

        for (File file : failed) {
            try {
                this.storageService.delete(file.getS3Key());
                file.setS3Key(null);
            } catch (Exception e) {
                log.error("Error while deleting failed objects", e);
            }
        }

        this.fileRepository.saveAll(failed);
    }
}
