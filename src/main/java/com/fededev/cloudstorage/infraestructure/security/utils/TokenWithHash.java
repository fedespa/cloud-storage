package com.fededev.cloudstorage.infraestructure.security.utils;

public record TokenWithHash(
        String rawToken,
        String hashedToken
) {
}
