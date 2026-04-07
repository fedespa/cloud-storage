package com.fededev.cloudstorage.sharing.controller;

import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.sharing.model.response.SharedLinkDto;
import com.fededev.cloudstorage.sharing.request.CreateSharedLinkRequest;
import com.fededev.cloudstorage.sharing.service.SharingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class ShareLinkController {

    private final SharingService sharingService;

    @PostMapping("/{fileId}/share")
    public ResponseEntity<SharedLinkDto> share(
            @PathVariable UUID fileId,
            @RequestBody CreateSharedLinkRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        SharedLinkDto dto = this.sharingService.shareFile(fileId, request, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

}
