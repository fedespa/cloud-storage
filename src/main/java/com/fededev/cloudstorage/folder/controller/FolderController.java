package com.fededev.cloudstorage.folder.controller;

import com.fededev.cloudstorage.folder.model.response.FolderDto;
import com.fededev.cloudstorage.folder.request.CreateFolderRequest;
import com.fededev.cloudstorage.folder.service.FolderService;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping("/folders")
    public ResponseEntity<FolderDto> createFolder(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateFolderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        FolderDto response = this.folderService.create(workspaceId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
