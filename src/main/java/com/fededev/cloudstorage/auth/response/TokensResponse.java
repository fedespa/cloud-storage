package com.fededev.cloudstorage.auth.response;

public record TokensResponse(
        String accessToken,
        String refreshToken
) {
}
