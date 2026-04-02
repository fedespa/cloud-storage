package com.fededev.cloudstorage.file.controller;

import com.fededev.cloudstorage.file.model.response.FileDownload;
import com.fededev.cloudstorage.file.request.MoveFileRequest;
import com.fededev.cloudstorage.file.service.FileService;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        this.fileService.softDelete(fileId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{fileId}/move")
    public ResponseEntity<Void> moveFile(
            @PathVariable UUID fileId,
            @RequestBody MoveFileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        this.fileService.moveFile(fileId, request, userDetails);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download-url")
    public ResponseEntity<FileDownload> getDownloadUrl(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails user) {
        String url = this.fileService.generatePresignedUrl(id, user.getId());

        return ResponseEntity.ok(new FileDownload(url));
    }

}
