package com.fededev.cloudstorage.cleanup;

import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StaleUploadCleanupJob {

    private final FileRepository fileRepository;
    private final WorkspaceRepository workspaceRepository;

    @Scheduled(fixedDelay = 20, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cleanStaleUploads(){
        Instant cutoff = Instant.now().minus(30, ChronoUnit.MINUTES);

        List<File> files = this.fileRepository.findStaleUploads(cutoff, Limit.of(300));

        if (files.isEmpty()) return;

        Map<UUID, Long> storageByWorkspace = files.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getWorkspace().getId(),
                        Collectors.summingLong(File::getSize)
                ));

        storageByWorkspace.forEach(this.workspaceRepository::decreaseUsedStorage);

        files.forEach(File::markAsFailed);
        this.fileRepository.saveAll(files);
    }

}
