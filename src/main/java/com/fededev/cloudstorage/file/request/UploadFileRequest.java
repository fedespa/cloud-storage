package com.fededev.cloudstorage.file.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UploadFileRequest(

        @NotBlank
        String filename,
        @NotBlank
        String contentType,

        @NotNull
        Long sizeBytes

) {
}
