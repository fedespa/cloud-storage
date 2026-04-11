package com.fededev.cloudstorage.file.request;

import java.util.UUID;

public record MoveFileRequest(
        UUID targetFolderId
) {
}
