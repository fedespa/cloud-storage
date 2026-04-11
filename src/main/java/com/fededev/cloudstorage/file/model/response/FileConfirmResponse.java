package com.fededev.cloudstorage.file.model.response;

import java.util.UUID;

public record FileConfirmResponse(
        UUID id,
        String status
) {
}
