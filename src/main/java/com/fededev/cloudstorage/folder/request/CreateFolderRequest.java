package com.fededev.cloudstorage.folder.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateFolderRequest(

        UUID parentId,

        @NotBlank
        String name

) {
}
