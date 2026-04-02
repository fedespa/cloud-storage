package com.fededev.cloudstorage.trash.controller.response;

public record TrashCleanupResult(
        int filesDeleted,
        int foldersDeleted
) {
}
