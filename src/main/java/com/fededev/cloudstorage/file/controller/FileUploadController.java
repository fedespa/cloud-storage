package com.fededev.cloudstorage.file.controller;

import com.fededev.cloudstorage.file.model.response.FileConfirmResponse;
import com.fededev.cloudstorage.file.model.response.InitUploadResponseDto;
import com.fededev.cloudstorage.file.request.UploadFileRequest;
import com.fededev.cloudstorage.file.service.UploadFileService;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileUploadController {

    private final UploadFileService uploadFileService;

    @PostMapping("/folders/{folderId}/files/init")
    public ResponseEntity<InitUploadResponseDto> uploadToFolder(
            @PathVariable UUID folderId,
            @Valid @RequestBody UploadFileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        InitUploadResponseDto response = this.uploadFileService.initiateUpload(null, folderId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/workspaces/{workspaceId}/files/init")
    public ResponseEntity<InitUploadResponseDto> uploadToRoot(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UploadFileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        InitUploadResponseDto response = this.uploadFileService.initiateUpload(workspaceId, null, request, userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/files/{fileId}/confirm")
    public ResponseEntity<FileConfirmResponse> confirmUpload(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FileConfirmResponse response = this.uploadFileService.confirm(fileId, userDetails);

        return ResponseEntity.ok(response);
    }

}
