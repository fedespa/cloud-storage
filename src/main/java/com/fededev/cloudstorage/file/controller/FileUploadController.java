package com.fededev.cloudstorage.file.controller;

import com.fededev.cloudstorage.file.model.response.FileDto;
import com.fededev.cloudstorage.file.request.UploadFileRequest;
import com.fededev.cloudstorage.file.service.FileService;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileService fileService;

    @PostMapping(value = "/folders/{folderId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto> uploadToFolder(
            @PathVariable UUID folderId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UploadFileRequest request = new UploadFileRequest(null, folderId, file);

        FileDto response = this.fileService.uploadFile(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/workspaces/{workspaceId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto> uploadToRoot(
            @PathVariable UUID workspaceId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        UploadFileRequest request = new UploadFileRequest(workspaceId, null, file);

        FileDto response = this.fileService.uploadFile(request, userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
