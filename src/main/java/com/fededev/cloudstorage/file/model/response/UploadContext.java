package com.fededev.cloudstorage.file.model.response;

import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.workspace.model.Workspace;

public record UploadContext(
        Workspace workspace,
        Folder folder
) {
}
