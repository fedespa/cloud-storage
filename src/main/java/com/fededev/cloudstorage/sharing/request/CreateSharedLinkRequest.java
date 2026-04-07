package com.fededev.cloudstorage.sharing.request;

import jakarta.validation.constraints.Positive;

public record CreateSharedLinkRequest(

        @Positive
        Long durationSeconds

) {}