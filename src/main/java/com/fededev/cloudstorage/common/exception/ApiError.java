package com.fededev.cloudstorage.common.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        String code,
        String message,
        Instant timestamp,
        Map<String, String> details
) {
}
