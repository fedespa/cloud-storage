package com.fededev.cloudstorage.trash.controller;

import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.trash.controller.response.TrashFolderResponse;
import com.fededev.cloudstorage.trash.service.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/workspaces")
@RestController
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

    @GetMapping("/{workspaceId}/trash")
    public ResponseEntity<TrashFolderResponse> listTrash(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) UUID folderId,
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable
    ){
        TrashFolderResponse response = this.trashService.listContent(workspaceId, folderId, user, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workspaceId}/trash")
    public ResponseEntity<Void> emptyTrash(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal CustomUserDetails user

    ) {
        this.trashService.emptyTrash(workspaceId, user);
        return ResponseEntity.ok().build();

    }

}
