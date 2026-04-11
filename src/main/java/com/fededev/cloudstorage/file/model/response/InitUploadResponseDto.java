package com.fededev.cloudstorage.file.model.response;

import java.util.Map;
import java.util.UUID;

public record InitUploadResponseDto(
        UUID id,
        String url,
        Map<String, String> fields
) {
}
