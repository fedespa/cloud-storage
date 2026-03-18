package com.fededev.cloudstorage.infraestructure.security;

import java.util.UUID;

public record CreateJWTTokenDto(

        UUID userId,
        String email,
        boolean deleted

) {
}
