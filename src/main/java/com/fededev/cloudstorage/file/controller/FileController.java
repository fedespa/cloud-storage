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
@RequestMapping("/api/workspaces/{workspaceId}")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto> uploadFile(
            @PathVariable UUID workspaceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) UUID folderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        UploadFileRequest request = new UploadFileRequest(workspaceId, folderId, file);

        FileDto response = this.fileService.uploadFile(request, userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
