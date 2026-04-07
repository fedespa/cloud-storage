package com.fededev.cloudstorage.sharing.controller;

import com.fededev.cloudstorage.file.model.response.FileWithUrlDto;
import com.fededev.cloudstorage.sharing.service.SharingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shared-links")
@RequiredArgsConstructor
public class SharedLinkController {

    private final SharingService sharingService;

    @GetMapping("/{token}")
    public ResponseEntity<FileWithUrlDto> download(@PathVariable String token) {
        FileWithUrlDto response = this.sharingService.resolve(token);

        return ResponseEntity.ok().body(response);
    }
}