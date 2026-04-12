package com.fededev.cloudstorage.folder.request;

import java.util.UUID;

public record MoveFolderRequest (
        UUID targetFolderId
) {
}
