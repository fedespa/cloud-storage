package com.fededev.cloudstorage.workspace.controller;

import com.fededev.cloudstorage.folder.model.response.FolderDto;
import com.fededev.cloudstorage.folder.model.response.FullFolderResponse;
import com.fededev.cloudstorage.folder.request.CreateFolderRequest;
import com.fededev.cloudstorage.folder.service.FolderService;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final FolderService folderService;

    @GetMapping("/content")
    public ResponseEntity<FullFolderResponse> getRootContent(
            @PathVariable UUID workspaceId,
            @Qualifier("folders") @PageableDefault(size = 10) Pageable folderPageable,
            @Qualifier("files") @PageableDefault(size = 20) Pageable filePageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        FullFolderResponse response = this.workspaceService.listRootContent(workspaceId, folderPageable, filePageable, userDetails);

        return ResponseEntity.ok(response);
    }

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
