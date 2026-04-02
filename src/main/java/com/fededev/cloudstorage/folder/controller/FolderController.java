package com.fededev.cloudstorage.folder.controller;

import com.fededev.cloudstorage.folder.model.response.FolderDto;
import com.fededev.cloudstorage.folder.model.response.FullFolderResponse;
import com.fededev.cloudstorage.folder.request.MoveFolderRequest;
import com.fededev.cloudstorage.folder.service.FolderService;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
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
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping("/{folderId}")
    public ResponseEntity<FullFolderResponse> listFolder(
            @PathVariable UUID folderId,
            @Qualifier("folders") @PageableDefault(size = 10) Pageable folderPage,
            @Qualifier("files") @PageableDefault(size = 10) Pageable filePage,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        FullFolderResponse response = this.folderService.listFolder(folderId, folderPage, filePage, userDetails);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{folderId}/move")
    public ResponseEntity<FolderDto> moveFolder(
            @PathVariable UUID folderId,
            @RequestBody MoveFolderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){

        FolderDto response = this.folderService.move(folderId, request, userDetails);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable UUID folderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){

        this.folderService.delete(folderId, userDetails);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
